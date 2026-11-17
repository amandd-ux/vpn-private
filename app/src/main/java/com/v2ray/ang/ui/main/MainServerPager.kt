package com.v2ray.ang.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.LocateTarget
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.dto.entities.SubscriptionUsageInfo
import com.v2ray.ang.ui.compose.LocalV2NgColors
import com.v2ray.ang.ui.compose.ReorderableGridItem
import com.v2ray.ang.ui.compose.ReorderableListItem
import com.v2ray.ang.ui.compose.ThemeManager
import com.v2ray.ang.ui.compose.V2NgCard
import com.v2ray.ang.ui.compose.V2NgPillBadge
import com.v2ray.ang.ui.compose.verticalScrollbar
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs

@Composable
fun GroupPagerPage(
    groupId: String,
    mainViewModel: MainViewModel,
    selectedGuid: String?,
    locateTarget: LocateTarget?,
    doubleColumnDisplay: Boolean,
    searchQuery: String,
    serverFilter: ServerFilter = ServerFilter.ALL,
    serverSort: ServerSort = ServerSort.DEFAULT,
    lazyListStates: MutableMap<String, LazyListState>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    onSelectServer: (String) -> Unit,
    onEditServer: (String, ProfileItem) -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit = { mainViewModel.onAction(MainAction.ToggleFavorite(it)) },
    usageInfo: SubscriptionUsageInfo?,
    groupTitle: String,
    onOpenLink: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    val groupStateFlow = remember(groupId) {
        mainViewModel.serverGroupState(groupId)
    }
    val groupState by groupStateFlow.collectAsStateWithLifecycle()
    val compactCards by ThemeManager.compactCards.collectAsStateWithLifecycle()
    val canReorder = groupId.isNotEmpty() &&
        searchQuery.isEmpty() &&
        serverFilter == ServerFilter.ALL &&
        serverSort == ServerSort.DEFAULT

    val actions = remember(
        onSelectServer,
        onEditServer,
        onShareServer,
        onMoreServer,
        onRemoveServer,
        onToggleFavorite,
    ) {
        ServerRowActions(
            select = onSelectServer,
            edit = onEditServer,
            share = onShareServer,
            more = onMoreServer,
            remove = onRemoveServer,
            favorite = onToggleFavorite,
        )
    }

    // The box floats over the list (the list scrolls underneath it), so the list is padded by the box height.
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    var infoHeightPx by remember { mutableIntStateOf(0) }
    val horizontalPad = if (doubleColumnDisplay) 12.dp else 16.dp
    val listPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection) + horizontalPad,
        top = contentPadding.calculateTopPadding() + 6.dp + if (usageInfo != null) with(density) { infoHeightPx.toDp() } else 0.dp,
        end = contentPadding.calculateEndPadding(layoutDirection) + horizontalPad,
        bottom = contentPadding.calculateBottomPadding() + 12.dp,
    )

    Box(Modifier.fillMaxSize()) {
        if (groupState.rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(listPadding),
                contentAlignment = Alignment.Center,
            ) {
                V2NgCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.v2ng_servers_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.v2ng_servers_empty_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            ServerListPage(
                rows = groupState.rows,
                selectedGuid = selectedGuid,
                locateTarget = locateTarget?.takeIf { it.groupId == groupId },
                canReorder = canReorder,
                doubleColumnDisplay = doubleColumnDisplay,
                compactCards = compactCards,
                groupId = groupId,
                lazyListStates = lazyListStates,
                lazyGridStates = lazyGridStates,
                actions = actions,
                onLocateHandled = { mainViewModel.onAction(MainAction.LocateHandled) },
                onMoveServer = { fromGuid, toGuid ->
                    mainViewModel.onAction(MainAction.MoveServer(groupId, fromGuid, toGuid))
                },
                contentPadding = listPadding,
            )
        }
        if (usageInfo != null) {
            SubscriptionInfoCard(
                info = usageInfo,
                fallbackTitle = groupTitle,
                onOpenLink = onOpenLink,
                onRefresh = { mainViewModel.onAction(MainAction.UpdateGroupSubscription(groupId)) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged { infoHeightPx = it.height },
            )
        }
    }
}

private class ServerRowActions(
    val select: (String) -> Unit,
    val edit: (String, ProfileItem) -> Unit,
    val share: (String, ProfileItem) -> Unit,
    val more: (String, ProfileItem) -> Unit,
    val remove: (String, String) -> Unit,
    val favorite: (String) -> Unit,
)

@Composable
private fun ServerListPage(
    rows: List<ServerRowUiModel>,
    selectedGuid: String?,
    locateTarget: LocateTarget?,
    canReorder: Boolean,
    doubleColumnDisplay: Boolean,
    compactCards: Boolean,
    groupId: String,
    lazyListStates: MutableMap<String, LazyListState>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    actions: ServerRowActions,
    onLocateHandled: () -> Unit,
    onMoveServer: (fromGuid: String, toGuid: String) -> Unit,
    contentPadding: PaddingValues,
) {
    val itemSpacing = if (compactCards) 7.dp else 10.dp

    if (doubleColumnDisplay) {
        val gridState = remember(groupId) {
            lazyGridStates.getOrPut(groupId) { LazyGridState() }
        }
        val reorderableGridState = if (canReorder) {
            rememberReorderableLazyGridState(gridState) { from, to ->
                // PattNG: by the keys of the rows, their guids, not by their positions.
                onMoveServer(
                    from.key as? String ?: return@rememberReorderableLazyGridState,
                    to.key as? String ?: return@rememberReorderableLazyGridState,
                )
            }
        } else null

        LocateTargetEffect(locateTarget, rows, gridState, onLocateHandled)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(gridState),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            itemsIndexed(items = rows, key = { _, item -> item.guid }) { _, row ->
                val content: @Composable () -> Unit = {
                    ServerListItem(
                        row = row,
                        isSelected = row.guid == selectedGuid,
                        doubleColumnDisplay = true,
                        compactCards = compactCards,
                        actions = actions,
                    )
                }
                if (canReorder && reorderableGridState != null) {
                    ReorderableItem(
                        reorderableGridState,
                        key = row.guid,
                    ) { isDragging ->
                        ReorderableGridItem(
                            scope = this,
                            isDragging = isDragging,
                        ) { content() }
                    }
                } else {
                    content()
                }
            }
        }
    } else {
        val listState = remember(groupId) {
            lazyListStates.getOrPut(groupId) { LazyListState() }
        }
        val reorderableState = if (canReorder) {
            rememberReorderableLazyListState(listState) { from, to ->
                // PattNG: by the keys of the rows, their guids, not by their positions.
                onMoveServer(
                    from.key as? String ?: return@rememberReorderableLazyListState,
                    to.key as? String ?: return@rememberReorderableLazyListState,
                )
            }
        } else null

        LocateTargetEffect(locateTarget, rows, listState, onLocateHandled)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(listState),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            itemsIndexed(items = rows, key = { _, item -> item.guid }) { _, row ->
                if (canReorder && reorderableState != null) {
                    ReorderableItem(
                        reorderableState,
                        key = row.guid,
                    ) { isDragging ->
                        ReorderableListItem(
                            scope = this,
                            isDragging = isDragging,
                        ) {
                            ServerListItem(
                                row = row,
                                isSelected = row.guid == selectedGuid,
                                doubleColumnDisplay = false,
                                compactCards = compactCards,
                                actions = actions,
                            )
                        }
                    }
                } else {
                    ServerListItem(
                        row = row,
                        isSelected = row.guid == selectedGuid,
                        doubleColumnDisplay = false,
                        compactCards = compactCards,
                        actions = actions,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocateTargetEffect(
    target: LocateTarget?,
    rows: List<ServerRowUiModel>,
    state: LazyListState,
    onHandled: () -> Unit,
) {
    if (target == null) return
    LaunchedEffect(target, rows) {
        val index = rows.indexOfFirst { it.guid == target.serverGuid }
        if (index < 0) return@LaunchedEffect
        state.scrollToItem(index, -state.layoutInfo.viewportSize.height / 3)
        onHandled()
    }
}

@Composable
private fun LocateTargetEffect(
    target: LocateTarget?,
    rows: List<ServerRowUiModel>,
    state: LazyGridState,
    onHandled: () -> Unit,
) {
    if (target == null) return
    LaunchedEffect(target, rows) {
        val index = rows.indexOfFirst { it.guid == target.serverGuid }
        if (index < 0) return@LaunchedEffect
        state.scrollToItem(index, -state.layoutInfo.viewportSize.height / 3)
        onHandled()
    }
}

@Composable
private fun ServerListItem(
    row: ServerRowUiModel,
    isSelected: Boolean,
    doubleColumnDisplay: Boolean,
    compactCards: Boolean,
    actions: ServerRowActions,
) {
    val colors = MaterialTheme.colorScheme
    val extended = LocalV2NgColors.current
    val testResult = if (row.testDelayMillis == 0L) {
        ""
    } else {
        stringResource(R.string.server_test_delay_value, row.testDelayMillis)
    }
    val selectedStateDescription = if (isSelected) {
        stringResource(R.string.acc_selected_server)
    } else {
        null
    }

    val containerColor = if (isSelected) colors.surfaceContainerHigh else colors.surfaceContainer
    val borderColor = if (isSelected) colors.primary.copy(alpha = 0.78f) else extended.cardBorder.copy(alpha = 0.68f)
    val verticalPad = if (compactCards) 10.dp else 13.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (selectedStateDescription != null) {
                    stateDescription = selectedStateDescription
                }
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable { actions.select(row.guid) },
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.35.dp else 0.8.dp, borderColor),
        shadowElevation = if (isSelected) 6.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = verticalPad, bottom = verticalPad),
            verticalAlignment = Alignment.Top,
        ) {
            // Flag / Protocol badge circle
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(if (compactCards) 36.dp else 40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) colors.primary.copy(alpha = 0.16f) else colors.surfaceContainerLow
                    )
                    .border(
                        width = 0.75.dp,
                        color = if (isSelected) colors.primary.copy(alpha = 0.55f) else extended.cardBorder.copy(alpha = 0.65f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val flag = row.locationHint.flagEmoji
                if (!flag.isNullOrBlank()) {
                    Text(text = flag, fontSize = if (compactCards) 16.sp else 18.sp)
                } else {
                    Text(
                        text = row.profile.configType.name.take(2),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = extended.monoFont,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.remarks,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall.copy(
                            lineBreak = LineBreak.Paragraph,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        ),
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    IconButton(
                        onClick = { actions.favorite(row.guid) },
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = if (row.isFavorite) Icons.Rounded.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(R.string.v2ng_acc_favorite),
                            tint = if (row.isFavorite) extended.connecting else colors.onSurfaceVariant,
                            modifier = Modifier.size(19.dp),
                        )
                    }

                    if (doubleColumnDisplay) {
                        IconButton(
                            onClick = { actions.more(row.guid, row.profile) },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert_24dp),
                                contentDescription = stringResource(R.string.acc_more),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { actions.share(row.guid, row.profile) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share_24dp),
                                contentDescription = stringResource(R.string.title_configuration_share),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(
                            onClick = { actions.edit(row.guid, row.profile) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit_24dp),
                                contentDescription = stringResource(R.string.acc_edit),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(
                            onClick = { actions.remove(row.guid, row.remarks) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_24dp),
                                contentDescription = stringResource(R.string.acc_delete),
                                tint = extended.error.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (row.subscriptionBadge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = row.subscriptionBadge.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = row.statistics,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDirection = TextDirection.Ltr,
                        ),
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val protocolMeta = listOfNotNull(
                        row.typeDescription.takeIf { it.isNotBlank() },
                        row.locationHint.countryLabel?.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                    Text(
                        text = protocolMeta,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (isSelected) {
                            V2NgPillBadge(
                                text = stringResource(R.string.v2ng_active_badge),
                                containerColor = colors.primaryContainer,
                                contentColor = colors.primary,
                                mono = true,
                            )
                        }
                        if (testResult.isNotEmpty()) {
                            val delayOk = row.testDelayMillis > 0L
                            V2NgPillBadge(
                                text = testResult,
                                containerColor = if (delayOk) extended.connectedContainer else extended.errorContainer,
                                contentColor = if (delayOk) extended.connected else extended.error,
                                mono = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

internal suspend fun PagerState.navigateToPageOptimized(
    targetPage: Int,
    animateAdjacentPage: Boolean = true,
) {
    if (pageCount <= 0) return
    val target = targetPage.coerceIn(0, pageCount - 1)
    val current = settledPage.coerceIn(0, pageCount - 1)
    if (target == current) return

    if (abs(target - current) == 1 && animateAdjacentPage) {
        animateScrollToPage(target)
    } else {
        scrollToPage(target)
    }
}
