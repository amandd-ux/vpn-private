package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class V2NgAccent(
    val key: String,
    val primaryDark: Color,
    val primaryLight: Color,
    val secondaryDark: Color,
    val secondaryLight: Color,
) {
    INDIGO(
        key = "indigo",
        primaryDark = Color(0xFF7B6EF6),
        primaryLight = Color(0xFF5046E5),
        secondaryDark = Color(0xFF10D9A0),
        secondaryLight = Color(0xFF059669),
    ),
    CYAN(
        key = "cyan",
        primaryDark = Color(0xFF22D3EE),
        primaryLight = Color(0xFF0284C7),
        secondaryDark = Color(0xFF6D5DF6),
        secondaryLight = Color(0xFF4F46E5),
    ),
    EMERALD(
        key = "emerald",
        primaryDark = Color(0xFF10D9A0),
        primaryLight = Color(0xFF059669),
        secondaryDark = Color(0xFF38BDF8),
        secondaryLight = Color(0xFF0284C7),
    ),
    AMBER(
        key = "amber",
        primaryDark = Color(0xFFF59E0B),
        primaryLight = Color(0xFFD97706),
        secondaryDark = Color(0xFF7B6EF6),
        secondaryLight = Color(0xFF5046E5),
    );

    companion object {
        fun fromKey(key: String?): V2NgAccent = entries.firstOrNull { it.key == key } ?: INDIGO
    }
}

@Immutable
data class V2NgExtendedColors(
    val connected: Color,
    val connectedContainer: Color,
    val connecting: Color,
    val connectingContainer: Color,
    val disconnected: Color,
    val error: Color,
    val errorContainer: Color,
    val downloadAccent: Color,
    val uploadAccent: Color,
    val cardBorder: Color,
    val heroGlowStart: Color,
    val heroGlowEnd: Color,
    val monoFont: FontFamily,
)

private val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

private val VazirmatnFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn, FontWeight.Medium),
    Font(R.font.vazirmatn, FontWeight.SemiBold),
    Font(R.font.vazirmatn, FontWeight.Bold),
)

val V2NgMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono, FontWeight.Bold),
)

private fun buildTypography(fontFamily: FontFamily): Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

private val V2NgShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private fun buildLightColorScheme(accent: V2NgAccent): ColorScheme = lightColorScheme(
    primary = accent.primaryLight,
    onPrimary = Color.White,
    primaryContainer = accent.primaryLight.copy(alpha = 0.13f),
    onPrimaryContainer = accent.primaryLight,
    secondary = accent.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = accent.secondaryLight.copy(alpha = 0.14f),
    onSecondaryContainer = accent.secondaryLight,
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFE11D48),
    errorContainer = Color(0xFFFFE4E6),
    onError = Color.White,
    onErrorContainer = Color(0xFF881337),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE8EDF6),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFDDE3EE),
    inverseSurface = Color(0xFF161E2E),
    inverseOnSurface = Color(0xFFF8FAFC),
    inversePrimary = accent.primaryDark,
    scrim = Color(0xFF000000),
    surfaceTint = accent.primaryLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5FA),
    surfaceContainer = Color(0xFFEAF0F7),
    surfaceContainerHigh = Color(0xFFE2E8F2),
    surfaceContainerHighest = Color(0xFFD8E0EC),
)

private fun buildDarkColorScheme(accent: V2NgAccent): ColorScheme = darkColorScheme(
    primary = accent.primaryDark,
    onPrimary = Color(0xFF090D16),
    primaryContainer = accent.primaryDark.copy(alpha = 0.18f),
    onPrimaryContainer = accent.primaryDark.copy(alpha = 0.95f),
    secondary = accent.secondaryDark,
    onSecondary = Color(0xFF052E24),
    secondaryContainer = accent.secondaryDark.copy(alpha = 0.16f),
    onSecondaryContainer = accent.secondaryDark,
    tertiary = Color(0xFF10D9A0),
    onTertiary = Color(0xFF003828),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = Color(0xFFFB7185),
    errorContainer = Color(0xFF4C0519),
    onError = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFFE4E6),
    background = Color(0xFF0B0F17),
    onBackground = Color(0xFFF3F6FC),
    surface = Color(0xFF111723),
    onSurface = Color(0xFFF3F6FC),
    surfaceVariant = Color(0xFF1E283B),
    onSurfaceVariant = Color(0xFF9BA8C2),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF232E42),
    inverseSurface = Color(0xFFF3F6FC),
    inverseOnSurface = Color(0xFF0B0F17),
    inversePrimary = accent.primaryLight,
    scrim = Color(0xFF000000),
    surfaceTint = accent.primaryDark,
    surfaceContainerLowest = Color(0xFF070A10),
    surfaceContainerLow = Color(0xFF111723),
    surfaceContainer = Color(0xFF161E2E),
    surfaceContainerHigh = Color(0xFF1D273B),
    surfaceContainerHighest = Color(0xFF26324B),
)

// Semantic Colors preserved for legacy PattNG sub-screens & shared components
val colorPing = Color(0xFF10B981)
val colorPingRed = Color(0xFFF43F5E)
val colorConfigType = Color(0xFF6D5DF6)
val colorFabActive = Color(0xFF10D9A0)
val colorFabInactiveLight = Color(0xFF94A3B8)
val colorFabInactiveDark = Color(0xFF475569)
val dividerColorLight = Color(0xFFE2E8F0)
val dividerColorDark = Color(0xFF232E42)

// Toast Colors 90%
val toastNormalBgLight = Color(0xE61E293B)
val toastNormalBgDark = Color(0xE6334155)
val toastSuccessBg = Color(0xE6059669)
val toastErrorBg = Color(0xE6E11D48)
val toastInfoBg = Color(0xE64F46E5)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = Color.White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    private val _accentColor = MutableStateFlow(
        V2NgAccent.fromKey(MmkvManager.decodeSettingsString(AppConfig.PREF_V2NG_ACCENT_COLOR, V2NgAccent.INDIGO.key))
    )
    val accentColor: StateFlow<V2NgAccent> = _accentColor.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_ANIMATIONS, true)
    )
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_HAPTICS, true)
    )
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _showLiveChart = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_SHOW_LIVE_CHART, true)
    )
    val showLiveChart: StateFlow<Boolean> = _showLiveChart.asStateFlow()

    private val _compactCards = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_COMPACT_CARDS, false)
    )
    val compactCards: StateFlow<Boolean> = _compactCards.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun setAccentColor(accent: V2NgAccent) {
        MmkvManager.encodeSettings(AppConfig.PREF_V2NG_ACCENT_COLOR, accent.key)
        _accentColor.value = accent
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_V2NG_ANIMATIONS, enabled)
        _animationsEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_V2NG_HAPTICS, enabled)
        _hapticsEnabled.value = enabled
    }

    fun setShowLiveChart(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_V2NG_SHOW_LIVE_CHART, enabled)
        _showLiveChart.value = enabled
    }

    fun setCompactCards(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_V2NG_COMPACT_CARDS, enabled)
        _compactCards.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, false)
        _accentColor.value = V2NgAccent.fromKey(
            MmkvManager.decodeSettingsString(AppConfig.PREF_V2NG_ACCENT_COLOR, V2NgAccent.INDIGO.key)
        )
        _animationsEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_ANIMATIONS, true)
        _hapticsEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_HAPTICS, true)
        _showLiveChart.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_SHOW_LIVE_CHART, true)
        _compactCards.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_V2NG_COMPACT_CARDS, false)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

val LocalV2NgColors = compositionLocalOf {
    V2NgExtendedColors(
        connected = Color(0xFF10D9A0),
        connectedContainer = Color(0x2210D9A0),
        connecting = Color(0xFFF59E0B),
        connectingContainer = Color(0x22F59E0B),
        disconnected = Color(0xFF64748B),
        error = Color(0xFFF43F5E),
        errorContainer = Color(0x22F43F5E),
        downloadAccent = Color(0xFF10D9A0),
        uploadAccent = Color(0xFF7B6EF6),
        cardBorder = Color(0xFF232E42),
        heroGlowStart = Color(0x337B6EF6),
        heroGlowEnd = Color(0x0010D9A0),
        monoFont = V2NgMonoFamily,
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val accent by ThemeManager.accentColor.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val activeLocale = configuration.locales.get(0)?.language.orEmpty()
    val usePersianTypography = activeLocale in setOf("fa", "ar", "bqi")

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkColorScheme(accent)
        else -> buildLightColorScheme(accent)
    }

    val extendedColors = if (darkTheme) {
        V2NgExtendedColors(
            connected = Color(0xFF10D9A0),
            connectedContainer = Color(0x1F10D9A0),
            connecting = Color(0xFFFBBF24),
            connectingContainer = Color(0x1FFBBF24),
            disconnected = Color(0xFF64748B),
            error = Color(0xFFFB7185),
            errorContainer = Color(0x26FB7185),
            downloadAccent = Color(0xFF10D9A0),
            uploadAccent = accent.primaryDark,
            cardBorder = Color(0xFF232E42),
            heroGlowStart = accent.primaryDark.copy(alpha = 0.24f),
            heroGlowEnd = accent.secondaryDark.copy(alpha = 0.06f),
            monoFont = V2NgMonoFamily,
        )
    } else {
        V2NgExtendedColors(
            connected = Color(0xFF059669),
            connectedContainer = Color(0x1A059669),
            connecting = Color(0xFFD97706),
            connectingContainer = Color(0x1AD97706),
            disconnected = Color(0xFF64748B),
            error = Color(0xFFE11D48),
            errorContainer = Color(0x1AE11D48),
            downloadAccent = Color(0xFF059669),
            uploadAccent = accent.primaryLight,
            cardBorder = Color(0xFFE2E8F0),
            heroGlowStart = accent.primaryLight.copy(alpha = 0.14f),
            heroGlowEnd = accent.secondaryLight.copy(alpha = 0.04f),
            monoFont = V2NgMonoFamily,
        )
    }

    val typography = buildTypography(if (usePersianTypography) VazirmatnFamily else PlusJakartaSansFamily)
    val snackbarController = rememberAppSnackbarController()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalV2NgColors provides extendedColors,
        LocalAppSnackbar provides snackbarController,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = V2NgShapes,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
