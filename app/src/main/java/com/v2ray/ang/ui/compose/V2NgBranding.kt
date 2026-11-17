package com.v2ray.ang.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Custom geometric v2Ng emblem rendered cleanly via Jetpack Compose Canvas.
 */
@Composable
fun V2NgLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.11f

        val shieldPath = Path().apply {
            moveTo(w * 0.16f, h * 0.24f)
            lineTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.24f)
            lineTo(w * 0.72f, h * 0.68f)
            lineTo(w * 0.50f, h * 0.89f)
            lineTo(w * 0.28f, h * 0.68f)
            close()
        }

        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(primaryColor.copy(alpha = 0.20f), secondaryColor.copy(alpha = 0.14f)),
                start = Offset.Zero,
                end = Offset(w, h),
            )
        )

        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(primaryColor, secondaryColor),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            ),
            style = Stroke(width = strokeWidth * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val vChevron = Path().apply {
            moveTo(w * 0.32f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.66f)
            lineTo(w * 0.68f, h * 0.36f)
        }
        drawPath(
            path = vChevron,
            brush = Brush.linearGradient(
                colors = listOf(secondaryColor, primaryColor),
                start = Offset(w * 0.3f, h * 0.3f),
                end = Offset(w * 0.7f, h * 0.7f),
            ),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawCircle(
            color = secondaryColor,
            radius = w * 0.055f,
            center = Offset(w * 0.50f, h * 0.28f),
        )
    }
}

@Composable
fun V2NgBrandLockup(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp,
    showTagline: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        V2NgLogoMark(size = logoSize)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "CORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = V2NgMonoFamily,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            if (showTagline) {
                Text(
                    text = stringResource(R.string.v2ng_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Sleek startup splash overlay shown briefly on cold launch.
 */
@Composable
fun V2NgSplashOverlay(
    animationsEnabled: Boolean,
    onFinished: () -> Unit,
) {
    var visible by remember { mutableStateOf(true) }
    var animateIn by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.88f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 420 else 0, easing = FastOutSlowInEasing),
        label = "splashScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 380 else 0),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        animateIn = true
        delay(if (animationsEnabled) 560L else 120L)
        visible = false
        delay(220L)
        onFinished()
    }

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(durationMillis = if (animationsEnabled) 240 else 60)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.background,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha),
            ) {
                V2NgLogoMark(size = 84.dp)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.v2ng_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

fun formatSessionDuration(elapsedMs: Long): String {
    val totalSeconds = (elapsedMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
