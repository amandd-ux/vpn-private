package com.v2ray.ang.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.SubscriptionUsageInfo
import com.v2ray.ang.extension.toTrafficString
import com.v2ray.ang.ui.compose.LocalV2NgColors
import com.v2ray.ang.ui.compose.ThemeManager
import com.v2ray.ang.ui.compose.V2NgCard
import com.v2ray.ang.ui.compose.V2NgLogoMark
import com.v2ray.ang.ui.compose.V2NgPillBadge
import com.v2ray.ang.ui.compose.formatSessionDuration
import com.v2ray.ang.ui.compose.formatSpeedBps
import com.v2ray.ang.ui.compose.formatTrafficBytes
import com.v2ray.ang.ui.compose.v2ngScreenBackground
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class HomeConnectionPhase {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR,
}

@Composable
fun V2NgHomeScreen(
    uiState: MainUiState,
    onAction: (MainAction) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToServers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val animationsEnabled by ThemeManager.animationsEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by ThemeManager.hapticsEnabled.collectAsStateWithLifecycle()
    val showLiveChart by ThemeManager.showLiveChart.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val phase = when {
        uiState.isDisconnecting -> HomeConnectionPhase.DISCONNECTING
        uiState.isRunning && uiState.status is MainStatus.Connecting -> HomeConnectionPhase.CONNECTING
        uiState.isRunning -> HomeConnectionPhase.CONNECTED
        !uiState.connectionError.isNullOrBlank() -> HomeConnectionPhase.ERROR
        else -> HomeConnectionPhase.DISCONNECTED
    }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uiState.isRunning, uiState.trafficSnapshot.sessionStartMs) {
        if (uiState.isRunning) {
            while (true) {
                nowMs = System.currentTimeMillis()
                delay(1_000L)
            }
        } else {
            nowMs = System.currentTimeMillis()
        }
    }

    val elapsedSessionMs = if (uiState.isRunning && uiState.trafficSnapshot.sessionStartMs > 0L) {
        (nowMs - uiState.trafficSnapshot.sessionStartMs).coerceAtLeast(0L)
    } else {
        0L
    }

    val selectedSummary = uiState.selectedServerSummary
    val matchedGroupUsage: Pair<String, SubscriptionUsageInfo>? = remember(uiState.groups, selectedSummary) {
        val groupId = selectedSummary?.subscriptionId.orEmpty()
        val group = if (groupId.isNotEmpty()) {
            uiState.groups.firstOrNull { it.id == groupId && it.usageInfo != null }
        } else {
            uiState.groups.firstOrNull { it.usageInfo != null }
        }
        val usage = group?.usageInfo ?: return@remember null
        group.remarks to usage
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .v2ngScreenBackground(ambientGlow = true)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeHeaderRow(
                vpnMode = uiState.vpnMode,
                isRootMode = uiState.isRootMode,
                onOpenDrawer = onOpenDrawer,
            )

            Spacer(modifier = Modifier.height(16.dp))

            CentralPowerHero(
                phase = phase,
                animationsEnabled = animationsEnabled,
                onClick = {
                    if (hapticsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onAction(MainAction.ToggleService)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            HomeConnectionStatusBlock(
                phase = phase,
                uiState = uiState,
                onTestLatency = { onAction(MainAction.TestCurrentServer) },
                onDismissError = { onAction(MainAction.ClearConnectionError) },
            )

            Spacer(modifier = Modifier.height(22.dp))

            ServerLocationCard(
                summary = selectedSummary,
                onClick = onNavigateToServers,
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConnectionInfoCard(
                uiState = uiState,
                elapsedSessionMs = elapsedSessionMs,
                showLiveMetrics = showLiveChart,
                subscriptionUsage = matchedGroupUsage,
                onTestLatency = { onAction(MainAction.TestCurrentServer) },
                onEnableStats = { onAction(MainAction.EnableSpeedStats) },
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HomeHeaderRow(
    vpnMode: Boolean,
    isRootMode: Boolean,
    onOpenDrawer: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val modeLabel = stringResource(
        when {
            isRootMode -> R.string.v2ng_mode_root
            vpnMode -> R.string.v2ng_mode_vpn
            else -> R.string.v2ng_mode_proxy
        }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            V2NgLogoMark(size = 34.dp)
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
            )
            V2NgPillBadge(
                text = modeLabel,
                containerColor = colors.surfaceContainerHigh,
                contentColor = colors.onSurfaceVariant,
                borderColor = LocalV2NgColors.current.cardBorder.copy(alpha = 0.65f),
            )
        }

        Surface(
            shape = CircleShape,
            color = colors.surfaceContainer.copy(alpha = 0.85f),
            border = BorderStroke(0.8.dp, LocalV2NgColors.current.cardBorder.copy(alpha = 0.65f)),
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_menu_button"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = stringResource(R.string.acc_open_menu),
                    tint = colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CentralPowerHero(
    phase: HomeConnectionPhase,
    animationsEnabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current

    val accentColor by animateColorAsState(
        targetValue = when (phase) {
            HomeConnectionPhase.CONNECTED -> extended.connected
            HomeConnectionPhase.CONNECTING,
            HomeConnectionPhase.DISCONNECTING -> extended.connecting
            HomeConnectionPhase.ERROR -> extended.error
            HomeConnectionPhase.DISCONNECTED -> colors.primary
        },
        animationSpec = tween(350),
        label = "heroAccent",
    )

    val ringBaseColor by animateColorAsState(
        targetValue = when (phase) {
            HomeConnectionPhase.CONNECTED -> extended.connected
            HomeConnectionPhase.CONNECTING,
            HomeConnectionPhase.DISCONNECTING -> extended.connecting
            HomeConnectionPhase.ERROR -> extended.error
            HomeConnectionPhase.DISCONNECTED -> colors.primary
        },
        animationSpec = tween(350),
        label = "heroRingColor",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (phase == HomeConnectionPhase.CONNECTING || phase == HomeConnectionPhase.DISCONNECTING) 1100 else 2400,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart,
        ),
        label = "connectingSweep",
    )

    val activePulse = if (animationsEnabled && (phase == HomeConnectionPhase.CONNECTED || phase == HomeConnectionPhase.CONNECTING)) {
        pulseProgress
    } else {
        1f
    }

    val buttonScale by animateFloatAsState(
        targetValue = if (phase == HomeConnectionPhase.CONNECTED) 1.02f else 1f,
        animationSpec = tween(260),
        label = "buttonScale",
    )

    val connectDesc = stringResource(R.string.v2ng_acc_connect_button)

    Box(
        modifier = Modifier.size(264.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle ambient glow & 3 concentric decorative rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringBaseColor.copy(alpha = if (phase == HomeConnectionPhase.DISCONNECTED) 0.16f else 0.26f),
                        colors.secondary.copy(alpha = 0.06f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = maxRadius * activePulse,
                ),
                radius = maxRadius * activePulse,
                center = center,
            )

            // Outer ring
            drawCircle(
                color = ringBaseColor.copy(alpha = if (phase == HomeConnectionPhase.CONNECTED) 0.22f else 0.11f),
                radius = maxRadius * 0.96f * activePulse,
                center = center,
                style = Stroke(width = 1.1.dp.toPx()),
            )
            // Middle ring
            drawCircle(
                color = ringBaseColor.copy(alpha = if (phase == HomeConnectionPhase.CONNECTED) 0.34f else 0.18f),
                radius = maxRadius * 0.81f * (1f + (activePulse - 1f) * 0.55f),
                center = center,
                style = Stroke(width = 1.25.dp.toPx()),
            )
            // Inner ring
            drawCircle(
                color = ringBaseColor.copy(alpha = if (phase == HomeConnectionPhase.CONNECTED) 0.50f else 0.28f),
                radius = maxRadius * 0.67f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx()),
            )

            if (phase == HomeConnectionPhase.CONNECTING || phase == HomeConnectionPhase.DISCONNECTING) {
                val arcRadius = maxRadius * 0.67f
                drawArc(
                    color = accentColor,
                    startAngle = sweepAngle,
                    sweepAngle = 82f,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = androidx.compose.ui.geometry.Size(arcRadius * 2f, arcRadius * 2f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }

        // Core circular dark-navy power button
        Surface(
            modifier = Modifier
                .size(146.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .semantics { contentDescription = connectDesc }
                .clickable(role = Role.Button, onClick = onClick)
                .testTag("connect_button"),
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = if (phase == HomeConnectionPhase.CONNECTED) 16.dp else 10.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.surfaceContainerHigh,
                                colors.surfaceContainerLowest,
                            )
                        )
                    )
                    .border(
                        width = 1.6.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (phase == HomeConnectionPhase.DISCONNECTED) 0.55f else 0.92f),
                                colors.secondary.copy(alpha = 0.28f),
                            )
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = if (phase == HomeConnectionPhase.DISCONNECTED) colors.onSurface else accentColor,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeConnectionStatusBlock(
    phase: HomeConnectionPhase,
    uiState: MainUiState,
    onTestLatency: () -> Unit,
    onDismissError: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current

    val statusTitle = when (phase) {
        HomeConnectionPhase.CONNECTED -> stringResource(R.string.v2ng_status_connected)
        HomeConnectionPhase.CONNECTING -> stringResource(R.string.v2ng_status_connecting)
        HomeConnectionPhase.DISCONNECTING -> stringResource(R.string.v2ng_status_disconnecting)
        HomeConnectionPhase.ERROR -> stringResource(R.string.v2ng_status_error)
        HomeConnectionPhase.DISCONNECTED -> stringResource(R.string.v2ng_status_disconnected)
    }

    val titleColor = when (phase) {
        HomeConnectionPhase.CONNECTED -> extended.connected
        HomeConnectionPhase.CONNECTING,
        HomeConnectionPhase.DISCONNECTING -> extended.connecting
        HomeConnectionPhase.ERROR -> extended.error
        HomeConnectionPhase.DISCONNECTED -> colors.onBackground
    }

    val subtitleText = when {
        phase == HomeConnectionPhase.ERROR && !uiState.connectionError.isNullOrBlank() ->
            uiState.connectionError
        phase == HomeConnectionPhase.CONNECTING && uiState.status is MainStatus.Connecting && uiState.status.message.isNotBlank() ->
            uiState.status.message
        phase == HomeConnectionPhase.CONNECTED ->
            stringResource(R.string.v2ng_tap_to_disconnect)
        uiState.selectedServerSummary == null ->
            stringResource(R.string.v2ng_select_server_hint)
        else ->
            stringResource(R.string.v2ng_tap_to_connect)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = statusTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        AnimatedVisibility(visible = phase == HomeConnectionPhase.CONNECTED) {
            val testStatus = uiState.status
            val badgeText = when {
                uiState.isTesting -> stringResource(R.string.connection_test_testing)
                testStatus is MainStatus.ConnectionTest && testStatus.result.delayMillis >= 0 -> {
                    val delayText = stringResource(R.string.server_test_delay_value, testStatus.result.delayMillis)
                    val country = testStatus.result.country?.takeIf { it.isNotBlank() }
                    if (country != null) "$delayText · $country" else delayText
                }
                else -> stringResource(R.string.v2ng_test_latency)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = CircleShape,
                color = colors.primaryContainer.copy(alpha = 0.72f),
                border = BorderStroke(0.75.dp, colors.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !uiState.isTesting, onClick = onTestLatency)
                    .testTag("home_test_latency_button"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = extended.monoFont,
                            textDirection = TextDirection.Ltr,
                        ),
                        color = colors.primary,
                    )
                }
            }
        }

        AnimatedVisibility(visible = phase == HomeConnectionPhase.ERROR) {
            TextButton(onClick = onDismissError) {
                Text(stringResource(R.string.v2ng_dismiss))
            }
        }
    }
}

@Composable
private fun ServerLocationCard(
    summary: SelectedServerSummary?,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current

    V2NgCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_server_card"),
        containerColor = colors.surfaceContainer,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh)
                    .border(0.8.dp, extended.cardBorder.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val flag = summary?.locationHint?.flagEmoji
                if (!flag.isNullOrBlank()) {
                    Text(
                        text = flag,
                        fontSize = 22.sp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.v2ng_selected_server),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = summary?.remarks?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.v2ng_no_server_selected),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = if (summary != null) {
                    val country = summary.locationHint.countryLabel
                    listOfNotNull(
                        summary.typeDescription.takeIf { it.isNotBlank() },
                        country?.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                        .ifBlank { summary.statistics }
                } else {
                    stringResource(R.string.v2ng_select_server_hint)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                if (summary != null && summary.testDelayMillis != 0L) {
                    val delayGood = summary.testDelayMillis > 0L
                    V2NgPillBadge(
                        text = stringResource(R.string.server_test_delay_value, summary.testDelayMillis),
                        containerColor = if (delayGood) extended.connectedContainer else extended.errorContainer,
                        contentColor = if (delayGood) extended.connected else extended.error,
                        mono = true,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ConnectionInfoCard(
    uiState: MainUiState,
    elapsedSessionMs: Long,
    showLiveMetrics: Boolean,
    subscriptionUsage: Pair<String, SubscriptionUsageInfo>?,
    onTestLatency: () -> Unit,
    onEnableStats: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val snapshot = uiState.trafficSnapshot

    V2NgCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.surfaceContainer,
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.v2ng_connection_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
            Text(
                text = if (uiState.isRunning) {
                    stringResource(R.string.v2ng_status_connected)
                } else {
                    stringResource(R.string.v2ng_ready_to_connect)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (uiState.isRunning) extended.connected else colors.onSurfaceVariant,
            )
        }

        if (subscriptionUsage != null) {
            val (fallbackTitle, usage) = subscriptionUsage
            val model = remember(usage) { buildSubscriptionInfoUiModel(usage, System.currentTimeMillis()) }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.75.dp, color = extended.cardBorder.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.title ?: fallbackTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                val expiryText = model.expireMillis?.let {
                    stringResource(R.string.v2ng_expires_on, Utils.formatTimestamp(it, "yyyy-MM-dd"))
                } ?: stringResource(R.string.v2ng_no_expiry)
                Text(
                    text = expiryText,
                    style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr),
                    color = if (model.expired) extended.error else colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            val totalText = if (model.totalBytes > 0L) {
                model.totalBytes.toTrafficString()
            } else {
                stringResource(R.string.sub_info_unlimited)
            }
            Text(
                text = stringResource(R.string.sub_info_traffic, model.usedBytes.toTrafficString(), totalText),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = extended.monoFont,
                    textDirection = TextDirection.Ltr,
                ),
                color = if (model.trafficLow) extended.error else colors.onSurfaceVariant,
            )

            model.trafficFraction?.let { fraction ->
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (model.trafficLow) extended.error else colors.primary,
                    trackColor = colors.surfaceContainerHigh,
                )
            }
        }

        if (showLiveMetrics) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.75.dp, color = extended.cardBorder.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricTile(
                    icon = Icons.Rounded.Schedule,
                    label = stringResource(R.string.v2ng_session_duration),
                    value = formatSessionDuration(elapsedSessionMs),
                    accentColor = colors.primary,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    icon = Icons.Rounded.ArrowDownward,
                    label = stringResource(R.string.v2ng_download_traffic),
                    value = if (uiState.isRunning && uiState.isSpeedStatsEnabled) {
                        formatSpeedBps(snapshot.currentDownBps)
                    } else {
                        formatTrafficBytes(snapshot.sessionDownloadBytes)
                    },
                    accentColor = extended.downloadAccent,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    icon = Icons.Rounded.ArrowUpward,
                    label = stringResource(R.string.v2ng_upload_traffic),
                    value = if (uiState.isRunning && uiState.isSpeedStatsEnabled) {
                        formatSpeedBps(snapshot.currentUpBps)
                    } else {
                        formatTrafficBytes(snapshot.sessionUploadBytes)
                    },
                    accentColor = extended.uploadAccent,
                    modifier = Modifier.weight(1f),
                )
            }

            if (uiState.isRunning && !uiState.isSpeedStatsEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(0.65.dp, extended.cardBorder.copy(alpha = 0.6f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.v2ng_live_stats_disabled_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TextButton(
                            onClick = onEnableStats,
                            modifier = Modifier.testTag("home_enable_stats_button"),
                        ) {
                            Text(stringResource(R.string.v2ng_enable_live_stats))
                        }
                    }
                }
            } else if (uiState.isRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(R.string.v2ng_total_traffic)}: ${formatTrafficBytes(snapshot.sessionTotalBytes)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = extended.monoFont,
                            textDirection = TextDirection.Ltr,
                        ),
                        color = colors.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = onTestLatency,
                        enabled = !uiState.isTesting,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    ) {
                        Text(stringResource(R.string.v2ng_test_latency))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(0.6.dp, extended.cardBorder.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = extended.monoFont,
                    fontWeight = FontWeight.SemiBold,
                    textDirection = TextDirection.Ltr,
                ),
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
