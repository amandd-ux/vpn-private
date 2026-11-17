package com.v2ray.ang.handler

import com.tencent.mmkv.MMKV
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.util.JsonUtil
import java.util.Locale

data class ServerLocationHint(
    val flagEmoji: String? = null,
    val countryLabel: String? = null,
)

/**
 * Multi-process MMKV store for non-invasive v2Ng server metadata:
 * favorites, recently used timestamps, and verified country hints from latency/IP checks.
 */
object ServerMetadataManager {

    private const val ID_V2NG_META = "V2NG_SERVER_META"
    private const val KEY_FAVORITES = "favorite_guids"
    private const val KEY_RECENT_PREFIX = "recent_"
    private const val KEY_COUNTRY_PREFIX = "country_"

    private val storage: MMKV by lazy {
        MMKV.mmkvWithID(ID_V2NG_META, MMKV.MULTI_PROCESS_MODE)
    }

    fun getFavoriteGuids(): Set<String> {
        val json = storage.decodeString(KEY_FAVORITES) ?: return emptySet()
        return JsonUtil.fromJsonSafe(json, Array<String>::class.java)?.toSet() ?: emptySet()
    }

    fun isFavorite(guid: String): Boolean = guid in getFavoriteGuids()

    fun toggleFavorite(guid: String): Boolean {
        if (guid.isBlank()) return false
        storage.lock()
        try {
            val set = getFavoriteGuids().toMutableSet()
            val added = if (guid in set) {
                set.remove(guid)
                false
            } else {
                set.add(guid)
                true
            }
            storage.encode(KEY_FAVORITES, JsonUtil.toJson(set.toList()))
            return added
        } finally {
            storage.unlock()
        }
    }

    fun recordServerUsed(guid: String) {
        if (guid.isBlank()) return
        storage.encode(KEY_RECENT_PREFIX + guid, System.currentTimeMillis())
    }

    fun getLastUsedTime(guid: String): Long {
        if (guid.isBlank()) return 0L
        return storage.decodeLong(KEY_RECENT_PREFIX + guid, 0L)
    }

    fun getAllRecentTimes(): Map<String, Long> {
        val keys = storage.allKeys() ?: return emptyMap()
        val result = mutableMapOf<String, Long>()
        for (key in keys) {
            if (!key.startsWith(KEY_RECENT_PREFIX)) continue
            val guid = key.removePrefix(KEY_RECENT_PREFIX)
            val ts = storage.decodeLong(key, 0L)
            if (ts > 0L) result[guid] = ts
        }
        return result
    }

    fun recordServerCountry(guid: String, country: String?) {
        if (guid.isBlank() || country.isNullOrBlank()) return
        storage.encode(KEY_COUNTRY_PREFIX + guid, country.trim())
    }

    fun getCachedCountry(guid: String): String? {
        if (guid.isBlank()) return null
        return storage.decodeString(KEY_COUNTRY_PREFIX + guid)?.takeIf { it.isNotBlank() }
    }

    fun removeServerMeta(guid: String) {
        if (guid.isBlank()) return
        storage.lock()
        try {
            val favs = getFavoriteGuids().toMutableSet()
            if (favs.remove(guid)) {
                storage.encode(KEY_FAVORITES, JsonUtil.toJson(favs.toList()))
            }
            storage.removeValuesForKeys(arrayOf(KEY_RECENT_PREFIX + guid, KEY_COUNTRY_PREFIX + guid))
        } finally {
            storage.unlock()
        }
    }

    /**
     * Derives a truthful location hint ONLY from real data:
     * 1) Verified IP-API country code/name recorded from a real test
     * 2) Explicit regional indicator flag emoji in the server's remarks
     * 3) Explicit Aether exit location country code (when not negated with '!')
     * Returns null fields when no real country info exists.
     */
    fun extractLocationHint(guid: String, profile: ProfileItem): ServerLocationHint {
        val cached = getCachedCountry(guid)
        if (!cached.isNullOrBlank()) {
            val iso = cached.uppercase(Locale.US).takeIf { it.length == 2 && it.all(Char::isLetter) }
            val flag = iso?.let(::isoToFlagEmoji)
            val label = iso?.let(::isoToDisplayCountry) ?: cached
            return ServerLocationHint(flagEmoji = flag, countryLabel = label)
        }

        val flagFromRemarks = extractFirstFlagEmoji(profile.remarks)
        if (flagFromRemarks != null) {
            val iso = flagEmojiToIso(flagFromRemarks)
            return ServerLocationHint(
                flagEmoji = flagFromRemarks,
                countryLabel = iso?.let(::isoToDisplayCountry)
            )
        }

        val exitLoc = profile.aetherExitLoc?.trim()
        if (!exitLoc.isNullOrEmpty() && !exitLoc.startsWith("!")) {
            val firstCode = exitLoc.split(',').firstOrNull()?.trim()?.uppercase(Locale.US)
            if (firstCode != null && firstCode.length == 2 && firstCode.all(Char::isLetter)) {
                return ServerLocationHint(
                    flagEmoji = isoToFlagEmoji(firstCode),
                    countryLabel = isoToDisplayCountry(firstCode)
                )
            }
        }

        return ServerLocationHint()
    }

    private fun extractFirstFlagEmoji(text: String): String? {
        if (text.isEmpty()) return null
        var i = 0
        while (i < text.length) {
            val cp1 = text.codePointAt(i)
            val nextIdx = i + Character.charCount(cp1)
            if (cp1 in 0x1F1E6..0x1F1FF && nextIdx < text.length) {
                val cp2 = text.codePointAt(nextIdx)
                if (cp2 in 0x1F1E6..0x1F1FF) {
                    return String(intArrayOf(cp1, cp2), 0, 2)
                }
            }
            i = nextIdx
        }
        return null
    }

    private fun flagEmojiToIso(flag: String): String? {
        if (flag.codePointCount(0, flag.length) != 2) return null
        val cp1 = flag.codePointAt(0)
        val cp2 = flag.codePointAt(Character.charCount(cp1))
        if (cp1 !in 0x1F1E6..0x1F1FF || cp2 !in 0x1F1E6..0x1F1FF) return null
        val c1 = ('A'.code + (cp1 - 0x1F1E6)).toChar()
        val c2 = ('A'.code + (cp2 - 0x1F1E6)).toChar()
        return "$c1$c2"
    }

    private fun isoToFlagEmoji(iso: String): String? {
        if (iso.length != 2 || !iso.all(Char::isLetter)) return null
        val upper = iso.uppercase(Locale.US)
        val cp1 = 0x1F1E6 + (upper[0].code - 'A'.code)
        val cp2 = 0x1F1E6 + (upper[1].code - 'A'.code)
        return String(intArrayOf(cp1, cp2), 0, 2)
    }

    private fun isoToDisplayCountry(iso: String): String? {
        val name = Locale.Builder().setRegion(iso).build().getDisplayCountry(Locale.getDefault())
        return name.takeIf { it.isNotBlank() && !it.equals(iso, ignoreCase = true) } ?: iso
    }
}
