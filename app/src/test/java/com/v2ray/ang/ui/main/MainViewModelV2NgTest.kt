package com.v2ray.ang.ui.main

import android.app.Application
import android.graphics.Bitmap
import com.v2ray.ang.dto.SubscriptionUpdateResult
import com.v2ray.ang.dto.TestServiceMessage
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.dto.entities.ServerAffiliationInfo
import com.v2ray.ang.dto.entities.ServersCache
import com.v2ray.ang.dto.entities.SubscriptionCache
import com.v2ray.ang.dto.entities.SubscriptionItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.handler.TrafficSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MainViewModelV2NgTest {

    private class FakeV2NgSource : MainDataSource {
        val events = Channel<MainServiceEvent>(Channel.UNLIMITED)
        var serverGuids = listOf("g_slow", "g_fast", "g_alpha")
        val remarksByGuid = mutableMapOf(
            "g_slow" to "Zeta Slow",
            "g_fast" to "Beta Fast",
            "g_alpha" to "Alpha Untested",
        )
        val delaysByGuid = mutableMapOf(
            "g_slow" to 850L,
            "g_fast" to 85L,
            "g_alpha" to 0L,
        )
        val favorites = linkedSetOf<String>()
        val recentTimes = linkedMapOf<String, Long>()
        var speedStatsFlag = false
        var trafficResetCount = 0
        var snapshot = TrafficSnapshot(
            sessionDownloadBytes = 2048L,
            sessionUploadBytes = 1024L,
            sessionStartMs = 1000L,
        )
        private var selectedSub = SUB
        private var selectedGuid: String? = "g_fast"

        override val mainServiceEvent: Flow<MainServiceEvent> = events.receiveAsFlow()
        override fun getSelectedSubscriptionId() = selectedSub
        override fun setSelectedSubscriptionId(id: String) {
            selectedSub = id
        }

        override fun getSelectServer(): String? = selectedGuid
        override fun setSelectServer(guid: String) {
            selectedGuid = guid
        }

        override fun getConfirmRemove() = false
        override fun getDoubleColumnDisplay() = false
        override fun isGroupAllDisplayEnabled() = false
        override fun getString(resId: Int) = "fallback_error"
        override fun getString(resId: Int, vararg formatArgs: Any) = ""
        override fun getStringArray(resId: Int) = emptyList<String>()
        override fun getSubscriptions() = listOf(SubscriptionCache(SUB, SubscriptionItem(remarks = "Sub")))
        override fun getSubscriptionItem(id: String): SubscriptionItem? = SubscriptionItem(remarks = "Sub")
        override fun getServerGuidList(groupId: String): List<String> = serverGuids
        override fun decodeServerConfig(guid: String): ProfileItem? =
            ProfileItem.create(EConfigType.VLESS).apply {
                subscriptionId = SUB
                remarks = remarksByGuid[guid] ?: guid
                server = "example.com"
                serverPort = "443"
            }

        override fun decodeAffiliationInfo(guid: String): ServerAffiliationInfo? =
            ServerAffiliationInfo(testDelayMillis = delaysByGuid[guid] ?: 0L)

        override fun moveServer(groupId: String, fromGuid: String, toGuid: String) = false
        override fun removeServer(guid: String) = true
        override fun removeAllServer() = 0
        override fun removeInvalidServerByGuid(guid: String) = 0
        override fun removeInvalidServersInGroup(groupId: String) = 0
        override fun clearAllTestDelayResults(guids: List<String>) = Unit
        override fun sortByTestResultsForSub(subId: String) = Unit
        override fun getSubsList() = listOf(SUB)
        override suspend fun importBatchConfig(server: String?, subscriptionId: String, updateUI: Boolean) = 0 to 0
        override fun updateConfigViaSubAll() = SubscriptionUpdateResult()
        override fun updateConfigViaSub(subscriptionCache: SubscriptionCache) = SubscriptionUpdateResult()
        override fun shareNonCustomConfigsToClipboard(guids: List<String>) = 0
        override fun share2QRCode(guid: String): Bitmap? = null
        override fun share2Clipboard(guid: String) = false
        override fun sendMsg2Service(msgId: Int, content: String) = Unit
        override fun queryServiceState() = Unit
        override fun sendMsg2TestService(msg: TestServiceMessage, requestId: String?) = Unit
        override fun cancelAllPing() = Unit
        override fun testCurrentServerRealPing(requestId: String) = Unit
        override fun syncSubscriptions() = Unit
        override fun initAssets() = Unit
        override fun close() = Unit

        override fun getTrafficSnapshot(): TrafficSnapshot = snapshot
        override fun isSpeedStatsEnabled(): Boolean = speedStatsFlag
        override fun setSpeedStatsEnabled(enabled: Boolean) {
            speedStatsFlag = enabled
        }

        override fun resetTrafficStats() {
            trafficResetCount++
            snapshot = TrafficSnapshot()
        }

        override fun getFavoriteGuids(): Set<String> = favorites.toSet()
        override fun toggleFavorite(guid: String): Boolean {
            return if (favorites.contains(guid)) {
                favorites.remove(guid)
                false
            } else {
                favorites.add(guid)
                true
            }
        }

        override fun getAllRecentTimes(): Map<String, Long> = recentTimes.toMap()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun disconnectingFlagAndConnectionErrorsFollowServiceLifecycle() = runBlocking {
        val source = FakeV2NgSource()
        val viewModel = MainViewModel(mock<Application>(), source)
        viewModel.awaitServers("g_slow", "g_fast", "g_alpha")

        source.events.send(MainServiceEvent.StateRunning)
        withTimeout(5_000) { viewModel.uiState.first { it.isRunning } }
        assertFalse(viewModel.uiState.value.isDisconnecting)

        viewModel.onAction(MainAction.MarkDisconnecting)
        assertTrue(viewModel.uiState.value.isDisconnecting)

        source.events.send(MainServiceEvent.StateStopSuccess)
        withTimeout(5_000) { viewModel.uiState.first { !it.isRunning } }
        assertFalse(viewModel.uiState.value.isDisconnecting)

        source.events.send(MainServiceEvent.StateStartFailure("Handshake timeout"))
        withTimeout(5_000) { viewModel.uiState.first { it.connectionError == "Handshake timeout" } }
        assertFalse(viewModel.uiState.value.isDisconnecting)

        viewModel.onAction(MainAction.ClearConnectionError)
        assertNull(viewModel.uiState.value.connectionError)

        source.events.send(MainServiceEvent.StateStartFailure("Bind error"))
        withTimeout(5_000) { viewModel.uiState.first { it.connectionError == "Bind error" } }
        source.events.send(MainServiceEvent.StateStartSuccess)
        withTimeout(5_000) { viewModel.uiState.first { it.isRunning && it.connectionError == null } }
    }

    @Test
    fun serverFiltersAndSortsTransformVisibleServerRows() = runBlocking {
        val source = FakeV2NgSource().apply {
            recentTimes["g_slow"] = 5000L
            recentTimes["g_alpha"] = 9000L
        }
        val viewModel = MainViewModel(mock<Application>(), source)
        viewModel.awaitServers("g_slow", "g_fast", "g_alpha")

        // Sort by latency puts fast (85ms), then slow (850ms), then untested (0ms)
        viewModel.onAction(MainAction.SetServerSort(ServerSort.LATENCY))
        viewModel.awaitServers("g_fast", "g_slow", "g_alpha")

        // Sort by name puts Alpha, Beta, Zeta
        viewModel.onAction(MainAction.SetServerSort(ServerSort.NAME))
        viewModel.awaitServers("g_alpha", "g_fast", "g_slow")

        // Filter FAST (<500ms and >0ms) leaves only g_fast
        viewModel.onAction(MainAction.SetServerFilter(ServerFilter.FAST))
        viewModel.awaitServers("g_fast")

        // Filter RECENT + sort RECENT orders most recently used first
        viewModel.onAction(MainAction.SetServerFilter(ServerFilter.RECENT))
        viewModel.onAction(MainAction.SetServerSort(ServerSort.RECENT))
        viewModel.awaitServers("g_alpha", "g_slow")

        // Toggle favorite and filter FAVORITES
        viewModel.onAction(MainAction.ToggleFavorite("g_slow"))
        withTimeout(5_000) { viewModel.uiState.first { "g_slow" in it.favoriteGuids } }
        viewModel.onAction(MainAction.SetServerFilter(ServerFilter.FAVORITES))
        viewModel.awaitServers("g_slow")

        viewModel.onAction(MainAction.ToggleFavorite("g_slow"))
        withTimeout(5_000) { viewModel.uiState.first { "g_slow" !in it.favoriteGuids } }
        viewModel.awaitServers()
    }

    @Test
    fun speedStatsActionsPersistSettingAndResetSnapshot() = runBlocking {
        val source = FakeV2NgSource()
        val viewModel = MainViewModel(mock<Application>(), source)
        viewModel.awaitServers("g_slow", "g_fast", "g_alpha")

        assertFalse(viewModel.uiState.value.isSpeedStatsEnabled)
        withTimeout(5_000) {
            viewModel.uiState.first { it.trafficSnapshot.sessionDownloadBytes == 2048L }
        }

        viewModel.onAction(MainAction.EnableSpeedStats)
        assertTrue(source.speedStatsFlag)
        assertTrue(viewModel.uiState.value.isSpeedStatsEnabled)

        viewModel.onAction(MainAction.SetSpeedStatsEnabled(false))
        assertFalse(source.speedStatsFlag)
        assertFalse(viewModel.uiState.value.isSpeedStatsEnabled)

        viewModel.onAction(MainAction.ResetTrafficStats)
        withTimeout(5_000) {
            viewModel.uiState.first { it.trafficSnapshot.sessionDownloadBytes == 0L }
        }
        assertEquals(1, source.trafficResetCount)
    }

    private suspend fun MainViewModel.awaitServers(vararg guids: String, groupId: String = SUB) {
        withTimeout(5_000) {
            serverGroupState(groupId).first { state -> state.servers.map(ServersCache::guid) == guids.toList() }
        }
    }

    private companion object {
        const val SUB = "sub_1"
    }
}
