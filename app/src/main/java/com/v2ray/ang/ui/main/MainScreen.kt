package com.v2ray.ang.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.extension.toastError
import com.v2ray.ang.ui.compose.QRCodeDialog
import com.v2ray.ang.ui.compose.ThemeManager
import com.v2ray.ang.ui.compose.V2NgSplashOverlay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onExitToBackground: () -> Unit = {},
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val animationsEnabled by ThemeManager.animationsEnabled.collectAsStateWithLifecycle()
    val displayText = mainViewModel.formatStatus(uiState.status)
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    // PattNG: a move of a profile the storage refused is told once, each refusal by its number, so that one set again
    // right after the last was told is told too; the groups are shown anew, as stored.
    val context = LocalContext.current
    val failureText = stringResource(R.string.toast_failure)
    val moveRefusal = uiState.moveRefusal
    LaunchedEffect(moveRefusal) {
        if (moveRefusal != null) {
            context.toastError(failureText)
            onAction(MainAction.MoveRefusalShown(moveRefusal))
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(V2NgTab.Home) }
    var showSplash by rememberSaveable { mutableStateOf(true) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by rememberSaveable(stateSaver = ServerDeleteTarget.Saver) {
        mutableStateOf<ServerDeleteTarget?>(null)
    }
    var showDonate by rememberSaveable { mutableStateOf(false) }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    val removeServer: (String, String) -> Unit = { guid, profileName ->
        if (confirmRemove) {
            showRemoveConfirm = ServerDeleteTarget(guid, profileName)
        } else {
            onAction(MainAction.RemoveServer(guid))
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { groups.size.coerceAtLeast(1) },
    )

    val lazyListStates = remember { mutableStateMapOf<String, LazyListState>() }
    val lazyGridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    LaunchedEffect(groups) {
        val validGroupIds = groups.map { it.id }.toSet()
        lazyListStates.keys.retainAll(validGroupIds)
        lazyGridStates.keys.retainAll(validGroupIds)
    }

    LaunchedEffect(groups, uiState.selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == uiState.selectedGroupId }
            .takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    val latestGroups by rememberUpdatedState(groups)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val currentGroups = latestGroups
                if (page in currentGroups.indices) {
                    onAction(MainAction.SelectGroup(currentGroups[page].id))
                }
            }
    }

    BackHandler {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            showSearch && selectedTab == V2NgTab.Servers -> {
                searchQuery = ""
                onAction(MainAction.Search(""))
                showSearch = false
            }
            selectedTab != V2NgTab.Home -> selectedTab = V2NgTab.Home
            else -> onExitToBackground()
        }
    }

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) },
        showDonate = showDonate,
        onDismissDonate = { showDonate = false },
        onCopyDonationAddress = { address -> onAction(MainAction.CopyDonationAddress(address)) },
        onOpenAuthorTelegram = { onAction(MainAction.OpenAuthorTelegram) },
    )

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = more,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = removeServer,
        )
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                MainDrawerContent(
                    drawerState = drawerState,
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        onNavigate(route)
                    },
                )
            },
        ) {
            val renderTab: @Composable (Modifier) -> Unit = { contentModifier ->
                when (selectedTab) {
                    V2NgTab.Home -> V2NgHomeScreen(
                        uiState = uiState,
                        onAction = onAction,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToServers = { selectedTab = V2NgTab.Servers },
                        modifier = contentModifier,
                    )

                    V2NgTab.Servers -> V2NgServersScreen(
                        uiState = uiState,
                        displayText = displayText,
                        isLoading = isLoading,
                        showSearch = showSearch,
                        searchQuery = searchQuery,
                        pagerState = pagerState,
                        lazyListStates = lazyListStates,
                        lazyGridStates = lazyGridStates,
                        mainViewModel = mainViewModel,
                        onSearchQueryChange = { query ->
                            searchQuery = query
                            onAction(MainAction.Search(query))
                        },
                        onSearchClose = {
                            searchQuery = ""
                            onAction(MainAction.Search(""))
                            showSearch = false
                        },
                        onSearchToggle = { show -> showSearch = show },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onDonateClick = { showDonate = true },
                        onAction = onAction,
                        onMoreMenuAction = { menuAction ->
                            when (menuAction) {
                                MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
                                MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
                                MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
                                MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
                                MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
                                MainMoreMenuAction.LocateSelected -> onAction(MainAction.LocateSelectedServer)
                                MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
                                MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
                                MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
                                MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
                            }
                        },
                        onShareServer = { guid, profile, more ->
                            shareTarget = Triple(guid, profile, more)
                        },
                        onRemoveServer = removeServer,
                        modifier = contentModifier,
                    )

                    V2NgTab.Profiles -> V2NgProfilesScreen(
                        uiState = uiState,
                        mainViewModel = mainViewModel,
                        onAction = onAction,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onOpenSubscriptions = { onNavigate(MainDestination.Subscriptions) },
                        onShareServer = { guid, profile ->
                            shareTarget = Triple(guid, profile, false)
                        },
                        onRequestRemove = removeServer,
                        modifier = contentModifier,
                    )

                    V2NgTab.Stats -> V2NgStatsScreen(
                        uiState = uiState,
                        onAction = onAction,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        modifier = contentModifier,
                    )

                    V2NgTab.Settings -> V2NgSettingsEmbed(
                        uiState = uiState,
                        onAction = onAction,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigate = onNavigate,
                        modifier = contentModifier,
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                if (maxWidth >= 840.dp) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        V2NgNavigationRail(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                        )
                        renderTab(Modifier.weight(1f).fillMaxSize())
                    }
                } else {
                    Scaffold(
                        contentWindowInsets = WindowInsets(0),
                        bottomBar = {
                            V2NgBottomNavBar(
                                selectedTab = selectedTab,
                                onSelectTab = { selectedTab = it },
                            )
                        },
                    ) { innerPadding ->
                        renderTab(
                            Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding()),
                        )
                    }
                }
            }
        }

        if (showSplash) {
            V2NgSplashOverlay(
                animationsEnabled = animationsEnabled,
                onFinished = { showSplash = false },
            )
        }
    }
}
