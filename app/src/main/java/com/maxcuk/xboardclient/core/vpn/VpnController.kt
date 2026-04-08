package com.maxcuk.xboardclient.core.vpn

import android.content.Context
import android.content.Intent
import com.maxcuk.xboardclient.core.datastore.ConnectionPrefs
import com.maxcuk.xboardclient.core.proxy.ProxyRuntimeManager
import com.maxcuk.xboardclient.core.proxy.model.RuntimeStatus
import com.maxcuk.xboardclient.core.repository.NodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnController(
    private val context: Context,
    private val nodeRepository: NodeRepository,
    private val proxyRuntimeManager: ProxyRuntimeManager = ProxyRuntimeManager(context),
    private val connectionPrefs: ConnectionPrefs = ConnectionPrefs(context)
) {
    private val _state = MutableStateFlow(VpnConnectionState.DISCONNECTED)
    val state: StateFlow<VpnConnectionState> = _state.asStateFlow()

    suspend fun connectSelectedNode(): Result<Unit> {
        return runCatching {
            _state.value = VpnConnectionState.PREPARING
            val node = nodeRepository.currentSelectedNode() ?: error("请先选择节点")
            proxyRuntimeManager.prepare(node)
            check(proxyRuntimeManager.startPlaceholder()) { proxyRuntimeManager.runtimeStatus().message }
            context.startService(Intent(context, XBoardVpnService::class.java).apply {
                action = XBoardVpnService.ACTION_CONNECT
            })
            connectionPrefs.setLastConnected(true)
            _state.value = VpnConnectionState.CONNECTED
        }.onFailure {
            connectionPrefs.setLastConnected(false)
            _state.value = VpnConnectionState.ERROR
        }
    }

    suspend fun tryAutoReconnect(): Result<Unit> {
        if (!connectionPrefs.shouldAutoReconnect() || !connectionPrefs.wasLastConnected()) {
            return Result.success(Unit)
        }
        return connectSelectedNode()
    }

    fun runtimeInfo(): String = proxyRuntimeManager.runtimeInfo()

    fun runtimeStatus(): RuntimeStatus = proxyRuntimeManager.runtimeStatus()

    fun latestLogs(): String? = proxyRuntimeManager.latestLogs()

    suspend fun setAutoReconnect(enabled: Boolean) {
        connectionPrefs.setAutoReconnect(enabled)
    }

    fun disconnect() {
        context.startService(Intent(context, XBoardVpnService::class.java).apply {
            action = XBoardVpnService.ACTION_DISCONNECT
        })
        proxyRuntimeManager.stopPlaceholder()
        _state.value = VpnConnectionState.STOPPED
    }
}
