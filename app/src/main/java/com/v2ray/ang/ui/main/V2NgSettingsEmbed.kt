package com.v2ray.ang.ui.main

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tab
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppDivider
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.LocalV2NgColors
import com.v2ray.ang.ui.compose.SelectListDialog
import com.v2ray.ang.ui.compose.SettingsSwitchItem
import com.v2ray.ang.ui.compose.ThemeManager
import com.v2ray.ang.ui.compose.V2NgAccent
import com.v2ray.ang.ui.compose.V2NgCard
import com.v2ray.ang.ui.compose.V2NgSectionCard
import com.v2ray.ang.ui.compose.v2ngScreenBackground

private val toolDestinations = listOf(
    MainDestination.Subscriptions,
    MainDestination.Routing,
    MainDestination.PerAppProxy,
    MainDestination.UserAssets,
    MainDestination.BackupRestore,
    MainDestination.Logcat,
    MainDestination.CheckUpdate,
    MainDestination.TelegramChannel,
    MainDestination.About,
)

@StringRes
private fun V2NgAccent.labelRes(): Int = when (this) {
    V2NgAccent.INDIGO -> R.string.v2ng_accent_indigo
    V2NgAccent.CYAN -> R.string.v2ng_accent_cyan
    V2NgAccent.EMERALD -> R.string.v2ng_accent_emerald
    V2NgAccent.AMBER -> R.string.v2ng_accent_amber
}

@Composable
fun V2NgSettingsEmbed(
    uiState: MainUiState,
    onAction: (MainAction) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigate: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val isDark = LocalDarkTheme.current
    val scrollState = rememberScrollState()

    val themeMode by ThemeManager.themeMode.collectAsStateWithLifecycle()
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsStateWithLifecycle()
    val accent by ThemeManager.accentColor.collectAsStateWithLifecycle()
    val animationsEnabled by ThemeManager.animationsEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by ThemeManager.hapticsEnabled.collectAsStateWithLifecycle()
    val showLiveChart by ThemeManager.showLiveChart.collectAsStateWithLifecycle()
    val compactCards by ThemeManager.compactCards.collectAsStateWithLifecycle()

    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val languageEntries = stringArrayResource(R.array.language_select).toList()
    val languageValues = stringArrayResource(R.array.language_select_value).toList()
    val nightEntries = stringArrayResource(R.array.ui_mode_night).toList()
    val nightValues = stringArrayResource(R.array.ui_mode_night_value).toList()

    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        val currentIdx = languageValues.indexOf(uiState.languageCode).coerceAtLeast(0)
        SelectListDialog(
            title = stringResource(R.string.title_language),
            options = languageValues.indices.toList(),
            optionText = { idx -> languageEntries.getOrElse(idx) { languageValues[idx] } },
            selectedOption = currentIdx,
            showRadio = true,
            onSelected = { idx ->
                showLanguageDialog = false
                onAction(MainAction.SetLanguageCode(languageValues[idx]))
            },
            onDismiss = { showLanguageDialog = false },
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.v2ng_nav_settings),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground,
                )
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

            // Hero launcher for full SettingsActivity (DNS, TUN, MUX, Ports, Root)
            V2NgCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_core_settings_card"),
                containerColor = colors.surfaceContainerHigh,
                borderColor = colors.primary.copy(alpha = 0.55f),
                contentPadding = PaddingValues(16.dp),
                onClick = { onNavigate(MainDestination.Settings) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = colors.primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.v2ng_core_settings_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.v2ng_core_settings_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                }
            }

            SectionHeading(stringResource(R.string.v2ng_settings_appearance))
            V2NgSectionCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.title_pref_ui_mode_night),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        nightValues.forEachIndexed { index, value ->
                            val selected = themeMode == value
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { ThemeManager.setThemeMode(value) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) colors.primaryContainer else colors.surfaceContainerHigh,
                                border = BorderStroke(
                                    0.9.dp,
                                    if (selected) colors.primary else extended.cardBorder.copy(alpha = 0.6f),
                                ),
                            ) {
                                Text(
                                    text = nightEntries.getOrElse(index) { value },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) colors.primary else colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.v2ng_pref_accent_color),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        V2NgAccent.entries.forEach { item ->
                            val selected = accent == item
                            val swatch = if (isDark) item.primaryDark else item.primaryLight
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { ThemeManager.setAccentColor(item) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) swatch.copy(alpha = 0.16f) else colors.surfaceContainerHigh,
                                border = BorderStroke(
                                    width = if (selected) 1.3.dp else 0.75.dp,
                                    color = if (selected) swatch else extended.cardBorder.copy(alpha = 0.55f),
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(swatch),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = colors.onPrimary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = stringResource(item.labelRes()).substringBefore(' '),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) colors.onSurface else colors.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }

                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                if (dynamicSupported) {
                    SettingsSwitchItem(
                        title = stringResource(R.string.title_pref_dynamic_color),
                        summary = stringResource(R.string.summary_pref_dynamic_color),
                        checked = dynamicColor,
                        icon = rememberVectorPainter(Icons.Rounded.ColorLens),
                        onCheckedChange = { ThemeManager.setDynamicColorEnabled(it) },
                    )
                    AppDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                val selectedLangLabel = languageValues.indexOf(uiState.languageCode)
                    .takeIf { it >= 0 }
                    ?.let { languageEntries.getOrNull(it) }
                    ?: languageEntries.firstOrNull().orEmpty()

                SettingsActionRow(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.title_language),
                    subtitle = selectedLangLabel,
                    onClick = { showLanguageDialog = true },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.v2ng_pref_animations),
                    summary = stringResource(R.string.v2ng_summary_animations),
                    checked = animationsEnabled,
                    icon = rememberVectorPainter(Icons.Rounded.Animation),
                    onCheckedChange = { ThemeManager.setAnimationsEnabled(it) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.v2ng_pref_haptics),
                    summary = stringResource(R.string.v2ng_summary_haptics),
                    checked = hapticsEnabled,
                    icon = rememberVectorPainter(Icons.Rounded.Vibration),
                    onCheckedChange = { ThemeManager.setHapticsEnabled(it) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.v2ng_pref_show_live_chart),
                    summary = stringResource(R.string.v2ng_summary_show_live_chart),
                    checked = showLiveChart,
                    icon = rememberVectorPainter(Icons.Rounded.ShowChart),
                    onCheckedChange = { ThemeManager.setShowLiveChart(it) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.v2ng_pref_compact_cards),
                    summary = stringResource(R.string.v2ng_summary_compact_cards),
                    checked = compactCards,
                    icon = rememberVectorPainter(Icons.Rounded.ViewAgenda),
                    onCheckedChange = { ThemeManager.setCompactCards(it) },
                )
            }

            SectionHeading(stringResource(R.string.title_ui_settings))
            V2NgSectionCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchItem(
                    title = stringResource(R.string.title_pref_speed_enabled),
                    summary = stringResource(R.string.summary_pref_speed_enabled),
                    checked = uiState.isSpeedStatsEnabled,
                    icon = rememberVectorPainter(Icons.Rounded.Speed),
                    onCheckedChange = { onAction(MainAction.SetSpeedStatsEnabled(it)) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.title_pref_double_column_display),
                    summary = stringResource(R.string.summary_pref_double_column_display),
                    checked = uiState.doubleColumnDisplay,
                    icon = rememberVectorPainter(Icons.Rounded.GridView),
                    onCheckedChange = { onAction(MainAction.SetDoubleColumnDisplay(it)) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.title_pref_group_all_display),
                    summary = stringResource(R.string.summary_pref_group_all_display),
                    checked = uiState.groupAllDisplay,
                    icon = rememberVectorPainter(Icons.Rounded.Tab),
                    onCheckedChange = { onAction(MainAction.SetGroupAllDisplay(it)) },
                )
                AppDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.title_pref_confirm_remove),
                    summary = stringResource(R.string.summary_pref_confirm_remove),
                    checked = uiState.confirmRemove,
                    icon = rememberVectorPainter(Icons.Rounded.DeleteOutline),
                    onCheckedChange = { onAction(MainAction.SetConfirmRemove(it)) },
                )
            }

            SectionHeading(stringResource(R.string.v2ng_settings_tools))
            V2NgSectionCard(modifier = Modifier.fillMaxWidth()) {
                toolDestinations.forEachIndexed { index, dest ->
                    if (index > 0) {
                        AppDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(dest) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(dest.iconRes),
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = stringResource(dest.labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
        )
    }
}
