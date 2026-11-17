package com.v2ray.ang.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun Modifier.v2ngScreenBackground(ambientGlow: Boolean = true): Modifier {
    val extended = LocalV2NgColors.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val isDark = LocalDarkTheme.current

    return this
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    extended.bgTop,
                    extended.bgBottom,
                    extended.bgTop,
                )
            )
        )
        .then(
            if (ambientGlow) {
                Modifier.drawBehind {
                    val glowRadius = size.width.coerceAtLeast(360f) * 0.92f
                    val topCenter = Offset(size.width * 0.5f, size.height * 0.24f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = if (isDark) 0.14f else 0.08f),
                                secondary.copy(alpha = if (isDark) 0.05f else 0.02f),
                                Color.Transparent,
                            ),
                            center = topCenter,
                            radius = glowRadius,
                        ),
                        radius = glowRadius,
                        center = topCenter,
                    )
                }
            } else {
                Modifier
            }
        )
}

@Composable
fun V2NgCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    borderColor: Color = LocalV2NgColors.current.cardBorder.copy(alpha = 0.72f),
    borderWidth: Dp = 0.85.dp,
    shadowElevation: Dp = if (LocalDarkTheme.current) 6.dp else 2.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = shadowElevation,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun V2NgSectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = LocalV2NgColors.current.cardBorder.copy(alpha = 0.65f),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(0.8.dp, borderColor),
        shadowElevation = if (LocalDarkTheme.current) 3.dp else 1.dp,
    ) {
        Column(content = content)
    }
}

@Composable
fun V2NgPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    mono: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
        border = borderColor?.let { BorderStroke(0.75.dp, it) },
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = if (mono) LocalV2NgColors.current.monoFont else MaterialTheme.typography.labelSmall.fontFamily,
                fontWeight = FontWeight.SemiBold,
                textDirection = TextDirection.Ltr,
            ),
            color = contentColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

fun formatTrafficBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val unit = 1024.0
    if (bytes < unit) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(unit)).toInt().coerceAtMost(4)
    val prefix = "KMGT"[exp - 1]
    val value = bytes / unit.pow(exp.toDouble())
    return if (value >= 100.0) {
        String.format(Locale.US, "%.0f %sB", value, prefix)
    } else if (value >= 10.0) {
        String.format(Locale.US, "%.1f %sB", value, prefix)
    } else {
        String.format(Locale.US, "%.2f %sB", value, prefix)
    }
}

fun formatSpeedBps(bytesPerSecond: Long): String = "${formatTrafficBytes(bytesPerSecond)}/s"
