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
        primaryDark = Color(0xFF6E7BFF),
        primaryLight = Color(0xFF4F5CEB),
        secondaryDark = Color(0xFFA59BFF),
        secondaryLight = Color(0xFF6D5DF6),
    ),
    CYAN(
        key = "cyan",
        primaryDark = Color(0xFF38BDF8),
        primaryLight = Color(0xFF0284C7),
        secondaryDark = Color(0xFF818CF8),
        secondaryLight = Color(0xFF4F46E5),
    ),
    EMERALD(
        key = "emerald",
        primaryDark = Color(0xFF2DD4BF),
        primaryLight = Color(0xFF059669),
        secondaryDark = Color(0xFF60A5FA),
        secondaryLight = Color(0xFF0284C7),
    ),
    AMBER(
        key = "amber",
        primaryDark = Color(0xFFF59E0B),
        primaryLight = Color(0xFFD97706),
        secondaryDark = Color(0xFF818CF8),
        secondaryLight = Color(0xFF4F5CEB),
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
    val bgTop: Color,
    val bgBottom: Color,
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
    background = Color(0xFFF4F7FC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE6ECF7),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFD9E2F2),
    inverseSurface = Color(0xFF131F40),
    inverseOnSurface = Color(0xFFF8FAFC),
    inversePrimary = accent.primaryDark,
    scrim = Color(0xFF000000),
    surfaceTint = accent.primaryLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F4FB),
    surfaceContainer = Color(0xFFE9EFF9),
    surfaceContainerHigh = Color(0xFFE0E8F5),
    surfaceContainerHighest = Color(0xFFD5E0F0),
)

private fun buildDarkColorScheme(accent: V2NgAccent): ColorScheme = darkColorScheme(
    primary = accent.primaryDark,
    onPrimary = Color(0xFFF6F8FF),
    primaryContainer = accent.primaryDark.copy(alpha = 0.18f),
    onPrimaryContainer = Color(0xFFE3E8FF),
    secondary = accent.secondaryDark,
    onSecondary = Color(0xFF0B1124),
    secondaryContainer = accent.secondaryDark.copy(alpha = 0.17f),
    onSecondaryContainer = accent.secondaryDark,
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = Color(0xFF0A3A3A),
    onTertiaryContainer = Color(0xFF99F6E4),
    error = Color(0xFFFB7185),
    errorContainer = Color(0xFF4C0519),
    onError = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFFE4E6),
    background = Color(0xFF080E1E),
    onBackground = Color(0xFFF4F7FF),
    surface = Color(0xFF0B1328),
    onSurface = Color(0xFFF4F7FF),
    surfaceVariant = Color(0xFF182447),
    onSurfaceVariant = Color(0xFF92A3CB),
    outline = Color(0xFF4A5F94),
    outlineVariant = Color(0xFF26386B),
    inverseSurface = Color(0xFFF4F7FF),
    inverseOnSurface = Color(0xFF080E1E),
    inversePrimary = accent.primaryLight,
    scrim = Color(0xFF000000),
    surfaceTint = accent.primaryDark,
    surfaceContainerLowest = Color(0xFF050915),
    surfaceContainerLow = Color(0xFF0E1832),
    surfaceContainer = Color(0xFF131F40),
    surfaceContainerHigh = Color(0xFF18264E),
    surfaceContainerHighest = Color(0xFF203161),
)

// Semantic Colors preserved for legacy PattNG sub-screens & shared components
val colorPing = Color(0xFF2DD4BF)
val colorPingRed = Color(0xFFFB7185)
val colorConfigType = Color(0xFF818CF8)
val colorFabActive = Color(0xFF2DD4BF)
val colorFabInactiveLight = Color(0xFF94A3B8)
val colorFabInactiveDark = Color(0xFF3B4F82)
val dividerColorLight = Color(0xFFDCE4F2)
val dividerColorDark = Color(0xFF20315E)

// Toast Colors 90%
val toastNormalBgLight = Color(0xE61E293B)
val toastNormalBgDark = Color(0xE61A274A)
val toastSuccessBg = Color(0xE6059669)
val toastErrorBg = Color(0xE6E11D48)
val toastInfoBg = Color(0xE64F46E5)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = Color.White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "2") ?: "2"
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
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "2") ?: "2"
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

val LocalDarkTheme = compositionLocalOf { true }

val LocalV2NgColors = compositionLocalOf {
    V2NgExtendedColors(
        connected = Color(0xFF2DD4BF),
        connectedContainer = Color(0x202DD4BF),
        connecting = Color(0xFFFBBF24),
        connectingContainer = Color(0x20FBBF24),
        disconnected = Color(0xFF7B8DB8),
        error = Color(0xFFFB7185),
        errorContainer = Color(0x24FB7185),
        downloadAccent = Color(0xFF38BDF8),
        uploadAccent = Color(0xFF818CF8),
        cardBorder = Color(0xFF273A70),
        heroGlowStart = Color(0x386E7BFF),
        heroGlowEnd = Color(0x14A59BFF),
        bgTop = Color(0xFF080E1E),
        bgBottom = Color(0xFF0C152E),
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
            connected = Color(0xFF2DD4BF),
            connectedContainer = Color(0x1F2DD4BF),
            connecting = Color(0xFFFBBF24),
            connectingContainer = Color(0x1FFBBF24),
            disconnected = Color(0xFF7B8DB8),
            error = Color(0xFFFB7185),
            errorContainer = Color(0x26FB7185),
            downloadAccent = Color(0xFF38BDF8),
            uploadAccent = accent.primaryDark,
            cardBorder = Color(0xFF273A70),
            heroGlowStart = accent.primaryDark.copy(alpha = 0.28f),
            heroGlowEnd = accent.secondaryDark.copy(alpha = 0.08f),
            bgTop = Color(0xFF080E1E),
            bgBottom = Color(0xFF0D1734),
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
            downloadAccent = Color(0xFF0284C7),
            uploadAccent = accent.primaryLight,
            cardBorder = Color(0xFFD5E0F2),
            heroGlowStart = accent.primaryLight.copy(alpha = 0.14f),
            heroGlowEnd = accent.secondaryLight.copy(alpha = 0.05f),
            bgTop = Color(0xFFF4F7FC),
            bgBottom = Color(0xFFEAF0FA),
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
