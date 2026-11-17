package com.v2ray.ang.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.LocalV2NgColors
import com.v2ray.ang.ui.compose.v2ngScreenBackground
import kotlinx.coroutines.launch

@StringRes
private fun ServerFilter.labelRes(): Int = when (this) {
    ServerFilter.ALL -> R.string.v2ng_filter_all
    ServerFilter.FAVORITES -> R.string.v2ng_filter_favorites
    ServerFilter.RECENT -> R.string.v2ng_filter_recent
    ServerFilter.FAST -> R.string.v2ng_filter_fast
}

@StringRes
private fun ServerSort.labelRes(): Int = when (this) {
    ServerSort.DEFAULT -> R.string.v2ng_sort_default
    ServerSort.LATENCY -> R.string.v2ng_sort_latency
    ServerSort.NAME -> R.string.v2ng_sort_name
    ServerSort.RECENT -> R.string.v2ng_sort_recent
}

@Composable
fun V2NgServersScreen(
    uiState: MainUiState,
    displayText: String,
    isLoading: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    pagerState: PagerState,
    lazyListStates: MutableMap<String, LazyListState>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    mainViewModel: MainViewModel,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    onDonateClick: () -> Unit,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit,
    onShareServer: (String, ProfileItem, Boolean) -> Unit,
    onRemoveServer: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val groups = uiState.groups
    var floatingBarHeightPx by remember { mutableIntStateOf(0) }
    val floatingBarHeight = with(LocalDensity.current) { floatingBarHeightPx.toDp() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .v2ngScreenBackground(ambientGlow = false),
    ) {
        MainTopBar(
            isLoading = isLoading,
            showSearch = showSearch,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onSearchClose = onSearchClose,
            onSearchToggle = onSearchToggle,
            onMenuClick = onMenuClick,
            onDonateClick = onDonateClick,
            onAction = onAction,
            onMoreMenuAction = onMoreMenuAction,
        )

        ServerFilterSortRow(
            selectedFilter = uiState.serverFilter,
            selectedSort = uiState.serverSort,
            onSelectFilter = { onAction(MainAction.SetServerFilter(it)) },
            onSelectSort = { onAction(MainAction.SetServerSort(it)) },
        )

        if (groups.size > 1) {
            GroupTabBar(
                groups = groups,
                selectedTabIndex = pagerState.currentPage.coerceIn(0, groups.lastIndex),
                mainViewModel = mainViewModel,
                onTabClick = { targetIndex ->
                    scope.launch {
                        pagerState.navigateToPageOptimized(
                            targetPage = targetIndex,
                            animateAdjacentPage = true,
                        )
                    }
                },
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (groups.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    beyondViewportPageCount = 1,
                    key = { page -> groups.getOrNull(page)?.id ?: "group-page-$page" },
                ) { page ->
                    val group = groups.getOrNull(page) ?: return@HorizontalPager
                    GroupPagerPage(
                        groupId = group.id,
                        mainViewModel = mainViewModel,
                        selectedGuid = uiState.selectedGuid,
                        locateTarget = uiState.locateTarget,
                        doubleColumnDisplay = uiState.doubleColumnDisplay,
                        searchQuery = searchQuery,
                        serverFilter = uiState.serverFilter,
                        serverSort = uiState.serverSort,
                        lazyListStates = lazyListStates,
                        lazyGridStates = lazyGridStates,
                        onSelectServer = { guid -> onAction(MainAction.SelectServer(guid)) },
                        onEditServer = { guid, profile -> onAction(MainAction.EditServer(guid, profile)) },
                        onShareServer = { guid, profile -> onShareServer(guid, profile, false) },
                        onMoreServer = { guid, profile -> onShareServer(guid, profile, true) },
                        onRemoveServer = onRemoveServer,
                        onToggleFavorite = { guid -> onAction(MainAction.ToggleFavorite(guid)) },
                        usageInfo = group.usageInfo,
                        groupTitle = group.remarks,
                        onOpenLink = { url -> onAction(MainAction.OpenSubscriptionLink(url)) },
                        contentPadding = PaddingValues(bottom = floatingBarHeight + 12.dp),
                    )
                }
            }

            CompactConnectionDock(
                displayText = displayText,
                isRunning = uiState.isRunning,
                selectedServerName = uiState.selectedServerSummary?.remarks,
                onTestCurrent = { onAction(MainAction.TestCurrentServer) },
                onToggleService = { onAction(MainAction.ToggleService) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { floatingBarHeightPx = it.height },
            )
        }
    }
}

@Composable
private fun ServerFilterSortRow(
    selectedFilter: ServerFilter,
    selectedSort: ServerSort,
    onSelectFilter: (ServerFilter) -> Unit,
    onSelectSort: (ServerSort) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ServerFilter.entries.forEach { filter ->
                val selected = filter == selectedFilter
                FilterChip(
                    selected = selected,
                    onClick = { onSelectFilter(filter) },
                    label = {
                        Text(
                            text = stringResource(filter.labelRes()),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surfaceContainer,
                        labelColor = colors.onSurfaceVariant,
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.primary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = extended.cardBorder.copy(alpha = 0.65f),
                        selectedBorderColor = colors.primary.copy(alpha = 0.55f),
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            Surface(
                shape = CircleShape,
                color = colors.surfaceContainer,
                border = BorderStroke(0.75.dp, extended.cardBorder.copy(alpha = 0.65f)),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { sortMenuExpanded = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(selectedSort.labelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface,
                        maxLines = 1,
                    )
                }
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
                containerColor = colors.surfaceContainerHigh,
            ) {
                ServerSort.entries.forEach { sort ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(sort.labelRes()),
                                fontWeight = if (sort == selectedSort) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (sort == selectedSort) colors.primary else colors.onSurface,
                            )
                        },
                        onClick = {
                            sortMenuExpanded = false
                            onSelectSort(sort)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactConnectionDock(
    displayText: String,
    isRunning: Boolean,
    selectedServerName: String?,
    onTestCurrent: () -> Unit,
    onToggleService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val statusColor = if (isRunning) extended.connected else colors.onSurfaceVariant

    Surface(
        modifier = modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerHigh.copy(alpha = 0.96f),
        border = BorderStroke(
            0.9.dp,
            if (isRunning) extended.connected.copy(alpha = 0.45f) else extended.cardBorder.copy(alpha = 0.72f),
        ),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isRunning, onClick = onTestCurrent)
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!selectedServerName.isNullOrBlank()) {
                    Text(
                        text = selectedServerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = if (isRunning) extended.connected else colors.primary,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onToggleService)
                    .testTag("servers_quick_connect_button"),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start),
                        tint = if (isRunning) colors.onTertiary else colors.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
