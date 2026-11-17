package com.v2ray.ang.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.handler.DailyTrafficPoint
import com.v2ray.ang.handler.SpeedSample
import com.v2ray.ang.ui.compose.ConfirmDialog
import com.v2ray.ang.ui.compose.LocalV2NgColors
import com.v2ray.ang.ui.compose.V2NgCard
import com.v2ray.ang.ui.compose.V2NgPillBadge
import com.v2ray.ang.ui.compose.formatSessionDuration
import com.v2ray.ang.ui.compose.formatSpeedBps
import com.v2ray.ang.ui.compose.formatTrafficBytes
import com.v2ray.ang.ui.compose.v2ngScreenBackground
import kotlinx.coroutines.delay

@Composable
fun V2NgStatsScreen(
    uiState: MainUiState,
    onAction: (MainAction) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val snapshot = uiState.trafficSnapshot
    val scrollState = rememberScrollState()
    var showResetConfirm by remember { mutableStateOf(false) }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uiState.isRunning, snapshot.sessionStartMs) {
        if (uiState.isRunning) {
            while (true) {
                nowMs = System.currentTimeMillis()
                delay(1_000L)
            }
        }
    }
    val sessionDurationMs = if (uiState.isRunning && snapshot.sessionStartMs > 0L) {
        (nowMs - snapshot.sessionStartMs).coerceAtLeast(0L)
    } else {
        0L
    }

    if (showResetConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.v2ng_stats_reset),
            message = stringResource(R.string.v2ng_stats_reset_confirm),
            confirmText = stringResource(R.string.action_ok),
            onConfirm = { onAction(MainAction.ResetTrafficStats) },
            onDismiss = { showResetConfirm = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .v2ngScreenBackground(ambientGlow = false)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.v2ng_stats_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("stats_reset_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = stringResource(R.string.v2ng_stats_reset),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.acc_open_menu),
                            tint = colors.onSurface,
                        )
                    }
                }
            }

            if (!uiState.isSpeedStatsEnabled) {
                V2NgCard(
                    containerColor = colors.surfaceContainerHigh,
                    borderColor = colors.primary.copy(alpha = 0.5f),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.v2ng_live_stats_disabled_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { onAction(MainAction.EnableSpeedStats) },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.v2ng_enable_live_stats),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryStatCard(
                    title = stringResource(R.string.v2ng_stats_current_session),
                    primaryValue = formatTrafficBytes(snapshot.sessionTotalBytes),
                    secondaryValue = if (uiState.isRunning) {
                        formatSessionDuration(sessionDurationMs)
                    } else {
                        "↓ ${formatTrafficBytes(snapshot.sessionDownloadBytes)}  ↑ ${formatTrafficBytes(snapshot.sessionUploadBytes)}"
                    },
                    accentColor = colors.primary,
                    modifier = Modifier.weight(1f),
                )
                SummaryStatCard(
                    title = stringResource(R.string.v2ng_stats_today),
                    primaryValue = formatTrafficBytes(snapshot.todayTotalBytes),
                    secondaryValue = "↓ ${formatTrafficBytes(snapshot.todayDownloadBytes)}  ↑ ${formatTrafficBytes(snapshot.todayUploadBytes)}",
                    accentColor = extended.downloadAccent,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryStatCard(
                    title = stringResource(R.string.v2ng_stats_weekly),
                    primaryValue = formatTrafficBytes(snapshot.weeklyTotalBytes),
                    secondaryValue = "↓ ${formatTrafficBytes(snapshot.weeklyDownloadBytes)}  ↑ ${formatTrafficBytes(snapshot.weeklyUploadBytes)}",
                    accentColor = extended.connected,
                    modifier = Modifier.weight(1f),
                )
                SummaryStatCard(
                    title = stringResource(R.string.v2ng_stats_lifetime),
                    primaryValue = formatTrafficBytes(snapshot.lifetimeTotalBytes),
                    secondaryValue = "↓ ${formatTrafficBytes(snapshot.totalDownloadBytes)}  ↑ ${formatTrafficBytes(snapshot.totalUploadBytes)}",
                    accentColor = extended.uploadAccent,
                    modifier = Modifier.weight(1f),
                )
            }

            V2NgCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.v2ng_stats_live_throughput),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        V2NgPillBadge(
                            text = "↓ ${formatSpeedBps(snapshot.currentDownBps)}",
                            containerColor = extended.downloadAccent.copy(alpha = 0.16f),
                            contentColor = extended.downloadAccent,
                            mono = true,
                        )
                        V2NgPillBadge(
                            text = "↑ ${formatSpeedBps(snapshot.currentUpBps)}",
                            containerColor = extended.uploadAccent.copy(alpha = 0.16f),
                            contentColor = extended.uploadAccent,
                            mono = true,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                ThroughputSparkline(
                    samples = snapshot.recentSpeedSamples,
                    downloadColor = extended.downloadAccent,
                    uploadColor = extended.uploadAccent,
                    gridColor = extended.cardBorder.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
            }

            if (snapshot.weeklyDays.isNotEmpty() && snapshot.weeklyTotalBytes > 0L) {
                V2NgCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.v2ng_stats_weekly_chart),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    WeeklyUsageBarChart(
                        days = snapshot.weeklyDays,
                        downloadColor = extended.downloadAccent,
                        uploadColor = extended.uploadAccent,
                        trackColor = colors.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        snapshot.weeklyDays.forEach { day ->
                            Text(
                                text = day.dateIso.takeLast(5),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = extended.monoFont,
                                    textDirection = TextDirection.Ltr,
                                ),
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.75.dp, color = extended.cardBorder.copy(alpha = 0.45f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.v2ng_stats_daily_breakdown),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    snapshot.weeklyDays.reversed().filter { it.totalBytes > 0L }.forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = day.dateIso,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = extended.monoFont,
                                    textDirection = TextDirection.Ltr,
                                ),
                                color = colors.onSurface,
                            )
                            Text(
                                text = "↓ ${formatTrafficBytes(day.downloadBytes)}  ·  ↑ ${formatTrafficBytes(day.uploadBytes)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = extended.monoFont,
                                    textDirection = TextDirection.Ltr,
                                ),
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else if (snapshot.lifetimeTotalBytes == 0L && snapshot.sessionTotalBytes == 0L) {
                V2NgCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.v2ng_stats_no_data),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current

    V2NgCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(accentColor),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = primaryValue,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = extended.monoFont,
                fontWeight = FontWeight.Bold,
                textDirection = TextDirection.Ltr,
            ),
            color = colors.onSurface,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = secondaryValue,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = extended.monoFont,
                textDirection = TextDirection.Ltr,
            ),
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ThroughputSparkline(
    samples: List<SpeedSample>,
    downloadColor: Color,
    uploadColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rows = 3
        for (i in 0..rows) {
            val y = h * (i / rows.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 0.8.dp.toPx(),
            )
        }

        if (samples.size < 2) {
            drawLine(
                color = downloadColor.copy(alpha = 0.35f),
                start = Offset(0f, h * 0.85f),
                end = Offset(w, h * 0.85f),
                strokeWidth = 1.8.dp.toPx(),
            )
            return@Canvas
        }

        val maxBps = samples.maxOf { maxOf(it.downloadBps, it.uploadBps) }.coerceAtLeast(1024L).toFloat()
        val stepX = w / (samples.size - 1).coerceAtLeast(1)

        fun buildLinePath(selector: (SpeedSample) -> Long): Path = Path().apply {
            samples.forEachIndexed { idx, sample ->
                val x = idx * stepX
                val ratio = (selector(sample) / maxBps).coerceIn(0f, 1f)
                val y = h - ratio * (h * 0.86f) - h * 0.06f
                if (idx == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val downPath = buildLinePath { it.downloadBps }
        val fillPath = Path().apply {
            addPath(downPath)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(downloadColor.copy(alpha = 0.28f), Color.Transparent),
            ),
        )
        drawPath(
            path = downPath,
            color = downloadColor,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val upPath = buildLinePath { it.uploadBps }
        drawPath(
            path = upPath,
            color = uploadColor,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun WeeklyUsageBarChart(
    days: List<DailyTrafficPoint>,
    downloadColor: Color,
    uploadColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (days.isEmpty()) return@Canvas
        val maxDay = days.maxOf { it.totalBytes }.coerceAtLeast(1L).toFloat()
        val count = days.size
        val slotW = size.width / count
        val barW = (slotW * 0.44f).coerceAtLeast(8.dp.toPx())
        val radius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

        days.forEachIndexed { idx, day ->
            val left = idx * slotW + (slotW - barW) / 2f
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barW, size.height),
                cornerRadius = radius,
            )

            val totalRatio = (day.totalBytes / maxDay).coerceIn(0f, 1f)
            if (totalRatio > 0f) {
                val barH = (size.height * totalRatio).coerceAtLeast(6.dp.toPx())
                val top = size.height - barH
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(downloadColor, uploadColor),
                        startY = top,
                        endY = size.height,
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barW, barH),
                    cornerRadius = radius,
                )
            }
        }
    }
}
