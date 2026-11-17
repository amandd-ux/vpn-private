package com.v2ray.ang.ui.main

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.ui.compose.LocalV2NgColors
import com.v2ray.ang.ui.compose.SelectListDialog
import com.v2ray.ang.ui.compose.V2NgCard
import com.v2ray.ang.ui.compose.V2NgPillBadge
import com.v2ray.ang.ui.compose.v2ngScreenBackground

private data class ProtocolCreationOption(
    @StringRes val labelRes: Int,
    val typeValue: Int,
)

private val protocolCreationOptions = listOf(
    ProtocolCreationOption(R.string.menu_item_import_config_manually_vless, EConfigType.VLESS.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_vmess, EConfigType.VMESS.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_trojan, EConfigType.TROJAN.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_ss, EConfigType.SHADOWSOCKS.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_hysteria2, EConfigType.HYSTERIA2.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_wireguard, EConfigType.WIREGUARD.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_masque, EConfigType.MASQUE.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_aether, EConfigType.AETHER.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_socks, EConfigType.SOCKS.value),
    ProtocolCreationOption(R.string.menu_item_import_config_manually_http, EConfigType.HTTP.value),
    ProtocolCreationOption(R.string.menu_item_import_config_policy_group, EConfigType.POLICYGROUP.value),
    ProtocolCreationOption(R.string.menu_item_import_config_proxy_chain, EConfigType.PROXYCHAIN.value),
)

private enum class ProfileImportOption(@StringRes val labelRes: Int, val action: MainAction) {
    QRCode(R.string.menu_item_import_config_qrcode, MainAction.ImportQRcode),
    Clipboard(R.string.menu_item_import_config_clipboard, MainAction.ImportClipboard),
    LocalFile(R.string.menu_item_import_config_local, MainAction.ImportConfigLocal),
}

@Composable
fun V2NgProfilesScreen(
    uiState: MainUiState,
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onRequestRemove: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val activeGroupId = uiState.selectedGroupId
    val groupStateFlow = remember(activeGroupId) {
        mainViewModel.serverGroupState(activeGroupId)
    }
    val groupState by groupStateFlow.collectAsStateWithLifecycle()
    val rows = groupState.rows

    var showProtocolPicker by remember { mutableStateOf(false) }
    var showImportPicker by remember { mutableStateOf(false) }

    if (showProtocolPicker) {
        SelectListDialog(
            title = stringResource(R.string.v2ng_select_protocol),
            options = protocolCreationOptions,
            optionText = { stringResource(it.labelRes) },
            onSelected = { option ->
                showProtocolPicker = false
                onAction(MainAction.ImportManually(option.typeValue))
            },
            onDismiss = { showProtocolPicker = false },
        )
    }

    if (showImportPicker) {
        AlertDialog(
            onDismissRequest = { showImportPicker = false },
            title = { Text(stringResource(R.string.v2ng_import_profile)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileImportOption.entries.forEach { option ->
                        val icon = when (option) {
                            ProfileImportOption.QRCode -> Icons.Rounded.QrCodeScanner
                            ProfileImportOption.Clipboard -> Icons.Rounded.Add
                            ProfileImportOption.LocalFile -> Icons.Rounded.FileOpen
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.surfaceContainer,
                            border = BorderStroke(0.75.dp, extended.cardBorder.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    showImportPicker = false
                                    onAction(option.action)
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(icon, contentDescription = null, tint = colors.primary)
                                Text(
                                    text = stringResource(option.labelRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onSurface,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = colors.surfaceContainerHigh,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .v2ngScreenBackground(ambientGlow = false)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.v2ng_profiles_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground,
                    )
                    V2NgPillBadge(
                        text = stringResource(R.string.v2ng_profiles_count, rows.size),
                        containerColor = colors.primaryContainer,
                        contentColor = colors.primary,
                        mono = true,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { showProtocolPicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profiles_new_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.v2ng_create_profile),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                FilledTonalButton(
                    onClick = { showImportPicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profiles_import_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.v2ng_import_profile),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                OutlinedButton(
                    onClick = onOpenSubscriptions,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Rounded.Layers, contentDescription = stringResource(R.string.v2ng_manage_subscriptions), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    V2NgCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.v2ng_profiles_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.v2ng_profiles_empty_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items = rows, key = { it.guid }) { row ->
                        val isSelected = row.guid == uiState.selectedGuid
                        V2NgCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            containerColor = if (isSelected) colors.surfaceContainerHigh else colors.surfaceContainer,
                            borderColor = if (isSelected) {
                                colors.primary.copy(alpha = 0.72f)
                            } else {
                                extended.cardBorder.copy(alpha = 0.68f)
                            },
                            borderWidth = if (isSelected) 1.3.dp else 0.8.dp,
                            contentPadding = PaddingValues(14.dp),
                            onClick = { onAction(MainAction.EditServer(row.guid, row.profile)) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = row.remarks,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = row.statistics,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                V2NgPillBadge(
                                    text = row.profile.configType.name,
                                    containerColor = colors.primaryContainer,
                                    contentColor = colors.primary,
                                    mono = true,
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) extended.connectedContainer else colors.surfaceContainerHigh,
                                    border = BorderStroke(
                                        0.75.dp,
                                        if (isSelected) extended.connected.copy(alpha = 0.4f) else extended.cardBorder.copy(alpha = 0.55f),
                                    ),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable(enabled = !isSelected) {
                                            onAction(MainAction.SelectServer(row.guid))
                                        },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = extended.connected,
                                                modifier = Modifier.size(15.dp),
                                            )
                                        }
                                        Text(
                                            text = stringResource(
                                                if (isSelected) R.string.v2ng_in_use else R.string.v2ng_use_profile
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) extended.connected else colors.onSurface,
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onShareServer(row.guid, row.profile) },
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Share,
                                            contentDescription = stringResource(R.string.title_configuration_share),
                                            tint = colors.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    IconButton(
                                        onClick = { onAction(MainAction.EditServer(row.guid, row.profile)) },
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = stringResource(R.string.acc_edit),
                                            tint = colors.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    IconButton(
                                        onClick = { onRequestRemove(row.guid, row.remarks) },
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = stringResource(R.string.acc_delete),
                                            tint = extended.error.copy(alpha = 0.85f),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
