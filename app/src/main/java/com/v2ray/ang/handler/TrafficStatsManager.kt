package com.v2ray.ang.handler

import com.tencent.mmkv.MMKV
import com.v2ray.ang.util.JsonUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SpeedSample(
    val timestampMs: Long = 0L,
    val uploadBps: Long = 0L,
    val downloadBps: Long = 0L,
)

data class DailyTrafficPoint(
    val dateIso: String = "",
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
) {
    val totalBytes: Long get() = uploadBytes + downloadBytes
}

data class TrafficSnapshot(
    val sessionActive: Boolean = false,
    val sessionStartMs: Long = 0L,
    val sessionGuid: String = "",
    val sessionServerName: String = "",
    val sessionUploadBytes: Long = 0L,
    val sessionDownloadBytes: Long = 0L,
    val currentUpBps: Long = 0L,
    val currentDownBps: Long = 0L,
    val todayUploadBytes: Long = 0L,
    val todayDownloadBytes: Long = 0L,
    val totalUploadBytes: Long = 0L,
    val totalDownloadBytes: Long = 0L,
    val weeklyDays: List<DailyTrafficPoint> = emptyList(),
    val recentSpeedSamples: List<SpeedSample> = emptyList(),
) {
    val sessionTotalBytes: Long get() = sessionUploadBytes + sessionDownloadBytes
    val todayTotalBytes: Long get() = todayUploadBytes + todayDownloadBytes
    val lifetimeTotalBytes: Long get() = totalUploadBytes + totalDownloadBytes
    val weeklyUploadBytes: Long get() = weeklyDays.sumOf { it.uploadBytes }
    val weeklyDownloadBytes: Long get() = weeklyDays.sumOf { it.downloadBytes }
    val weeklyTotalBytes: Long get() = weeklyUploadBytes + weeklyDownloadBytes
}

/**
 * Multi-process MMKV-backed traffic telemetry store fed directly by Xray's real
 * outbound traffic stats in [NotificationManager] / [CoreServiceManager].
 * Uses its own MMKV namespace so existing PattNG stores remain untouched.
 */
object TrafficStatsManager {

    private const val ID_TRAFFIC_STATS = "V2NG_TRAFFIC_STATS"
    private const val KEY_SESSION_ACTIVE = "session_active"
    private const val KEY_SESSION_START_MS = "session_start_ms"
    private const val KEY_SESSION_GUID = "session_guid"
    private const val KEY_SESSION_SERVER_NAME = "session_server_name"
    private const val KEY_SESSION_UP = "session_up"
    private const val KEY_SESSION_DOWN = "session_down"
    private const val KEY_CURRENT_UP_BPS = "current_up_bps"
    private const val KEY_CURRENT_DOWN_BPS = "current_down_bps"
    private const val KEY_TOTAL_UP = "total_up"
    private const val KEY_TOTAL_DOWN = "total_down"
    private const val KEY_DAILY_PREFIX = "daily_"
    private const val KEY_SAMPLES = "recent_samples"
    private const val MAX_SAMPLES = 30
    private const val RETENTION_DAYS = 30L

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val storage: MMKV by lazy {
        MMKV.mmkvWithID(ID_TRAFFIC_STATS, MMKV.MULTI_PROCESS_MODE)
    }

    fun onSessionStarted(guid: String, serverName: String, isReload: Boolean = false) {
        storage.lock()
        try {
            val wasActive = storage.decodeBool(KEY_SESSION_ACTIVE, false)
            if (!isReload || !wasActive) {
                storage.encode(KEY_SESSION_START_MS, System.currentTimeMillis())
                storage.encode(KEY_SESSION_UP, 0L)
                storage.encode(KEY_SESSION_DOWN, 0L)
                storage.encode(KEY_SAMPLES, "[]")
            }
            storage.encode(KEY_SESSION_ACTIVE, true)
            storage.encode(KEY_SESSION_GUID, guid)
            storage.encode(KEY_SESSION_SERVER_NAME, serverName)
            storage.encode(KEY_CURRENT_UP_BPS, 0L)
            storage.encode(KEY_CURRENT_DOWN_BPS, 0L)
        } finally {
            storage.unlock()
        }
    }

    fun onSessionStopped() {
        storage.lock()
        try {
            storage.encode(KEY_SESSION_ACTIVE, false)
            storage.encode(KEY_CURRENT_UP_BPS, 0L)
            storage.encode(KEY_CURRENT_DOWN_BPS, 0L)
        } finally {
            storage.unlock()
        }
    }

    fun recordTrafficSample(
        uploadDelta: Long,
        downloadDelta: Long,
        uploadBps: Long,
        downloadBps: Long,
    ) {
        val safeUp = uploadDelta.coerceAtLeast(0L)
        val safeDown = downloadDelta.coerceAtLeast(0L)
        val safeUpBps = uploadBps.coerceAtLeast(0L)
        val safeDownBps = downloadBps.coerceAtLeast(0L)
        val todayKey = KEY_DAILY_PREFIX + LocalDate.now().format(dateFormatter)

        storage.lock()
        try {
            storage.encode(KEY_CURRENT_UP_BPS, safeUpBps)
            storage.encode(KEY_CURRENT_DOWN_BPS, safeDownBps)

            if (safeUp > 0L || safeDown > 0L) {
                storage.encode(KEY_SESSION_UP, storage.decodeLong(KEY_SESSION_UP, 0L) + safeUp)
                storage.encode(KEY_SESSION_DOWN, storage.decodeLong(KEY_SESSION_DOWN, 0L) + safeDown)
                storage.encode(KEY_TOTAL_UP, storage.decodeLong(KEY_TOTAL_UP, 0L) + safeUp)
                storage.encode(KEY_TOTAL_DOWN, storage.decodeLong(KEY_TOTAL_DOWN, 0L) + safeDown)

                val existingDaily = storage.decodeString(todayKey)
                    ?.let { JsonUtil.fromJsonSafe(it, DailyTrafficPoint::class.java) }
                    ?: DailyTrafficPoint(dateIso = LocalDate.now().format(dateFormatter))
                val updatedDaily = existingDaily.copy(
                    uploadBytes = existingDaily.uploadBytes + safeUp,
                    downloadBytes = existingDaily.downloadBytes + safeDown,
                )
                storage.encode(todayKey, JsonUtil.toJson(updatedDaily))
            }

            val samplesJson = storage.decodeString(KEY_SAMPLES)
            val samples = samplesJson
                ?.let { JsonUtil.fromJsonSafe(it, Array<SpeedSample>::class.java)?.toMutableList() }
                ?: mutableListOf()
            samples.add(
                SpeedSample(
                    timestampMs = System.currentTimeMillis(),
                    uploadBps = safeUpBps,
                    downloadBps = safeDownBps,
                )
            )
            while (samples.size > MAX_SAMPLES) {
                samples.removeAt(0)
            }
            storage.encode(KEY_SAMPLES, JsonUtil.toJson(samples))
            pruneOldDailyKeys()
        } finally {
            storage.unlock()
        }
    }

    fun getSnapshot(): TrafficSnapshot {
        val today = LocalDate.now()
        val todayIso = today.format(dateFormatter)
        storage.lock()
        try {
            val active = storage.decodeBool(KEY_SESSION_ACTIVE, false)
            val startMs = storage.decodeLong(KEY_SESSION_START_MS, 0L)
            val guid = storage.decodeString(KEY_SESSION_GUID, "").orEmpty()
            val serverName = storage.decodeString(KEY_SESSION_SERVER_NAME, "").orEmpty()
            val sessionUp = storage.decodeLong(KEY_SESSION_UP, 0L)
            val sessionDown = storage.decodeLong(KEY_SESSION_DOWN, 0L)
            val upBps = if (active) storage.decodeLong(KEY_CURRENT_UP_BPS, 0L) else 0L
            val downBps = if (active) storage.decodeLong(KEY_CURRENT_DOWN_BPS, 0L) else 0L
            val totalUp = storage.decodeLong(KEY_TOTAL_UP, 0L)
            val totalDown = storage.decodeLong(KEY_TOTAL_DOWN, 0L)

            val weeklyDays = (6 downTo 0).map { daysAgo ->
                val dateIso = today.minusDays(daysAgo.toLong()).format(dateFormatter)
                storage.decodeString(KEY_DAILY_PREFIX + dateIso)
                    ?.let { JsonUtil.fromJsonSafe(it, DailyTrafficPoint::class.java) }
                    ?.copy(dateIso = dateIso)
                    ?: DailyTrafficPoint(dateIso = dateIso, uploadBytes = 0L, downloadBytes = 0L)
            }
            val todayPoint = weeklyDays.lastOrNull { it.dateIso == todayIso } ?: DailyTrafficPoint(todayIso)
            val samples = storage.decodeString(KEY_SAMPLES)
                ?.let { JsonUtil.fromJsonSafe(it, Array<SpeedSample>::class.java)?.toList() }
                ?: emptyList()

            return TrafficSnapshot(
                sessionActive = active,
                sessionStartMs = startMs,
                sessionGuid = guid,
                sessionServerName = serverName,
                sessionUploadBytes = sessionUp,
                sessionDownloadBytes = sessionDown,
                currentUpBps = upBps,
                currentDownBps = downBps,
                todayUploadBytes = todayPoint.uploadBytes,
                todayDownloadBytes = todayPoint.downloadBytes,
                totalUploadBytes = totalUp,
                totalDownloadBytes = totalDown,
                weeklyDays = weeklyDays,
                recentSpeedSamples = samples,
            )
        } finally {
            storage.unlock()
        }
    }

    fun clearStatistics() {
        storage.lock()
        try {
            val dailyKeys = storage.allKeys()?.filter { it.startsWith(KEY_DAILY_PREFIX) }.orEmpty()
            if (dailyKeys.isNotEmpty()) {
                storage.removeValuesForKeys(dailyKeys.toTypedArray())
            }
            storage.encode(KEY_SESSION_UP, 0L)
            storage.encode(KEY_SESSION_DOWN, 0L)
            storage.encode(KEY_TOTAL_UP, 0L)
            storage.encode(KEY_TOTAL_DOWN, 0L)
            storage.encode(KEY_CURRENT_UP_BPS, 0L)
            storage.encode(KEY_CURRENT_DOWN_BPS, 0L)
            storage.encode(KEY_SAMPLES, "[]")
        } finally {
            storage.unlock()
        }
    }

    private fun pruneOldDailyKeys() {
        val cutoff = LocalDate.now().minusDays(RETENTION_DAYS)
        val keys = storage.allKeys() ?: return
        val expired = keys.filter { key ->
            if (!key.startsWith(KEY_DAILY_PREFIX)) return@filter false
            val dateStr = key.removePrefix(KEY_DAILY_PREFIX)
            val parsed = runCatching { LocalDate.parse(dateStr, dateFormatter) }.getOrNull() ?: return@filter true
            parsed.isBefore(cutoff)
        }
        if (expired.isNotEmpty()) {
            storage.removeValuesForKeys(expired.toTypedArray())
        }
    }
}
