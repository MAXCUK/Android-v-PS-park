package com.maxcuk.xboardclient.core.vpn

import android.content.Context
import android.content.Intent
import android.os.Build
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
            if (node.host.isBlank() || node.port <= 0) error("当前节点缺少有效地址或端口")
            when (node.type) {
                "shadowsocks" -> {
                    if (node.method.isNullOrBlank() || node.password.isNullOrBlank()) error("当前节点缺少 shadowsocks 必要参数")
                }
                "vless" -> {
                    if (node.uuid.isNullOrBlank()) error("当前节点缺少 VLESS 必要参数")
                }
            }
            val configFile = proxyRuntimeManager.prepare(node)
            check(proxyRuntimeManager.runtimeInstalled()) { proxyRuntimeManager.runtimeStatus().message }
            val intent = Intent(context, XBoardVpnService::class.java).apply {
                action = XBoardVpnService.ACTION_CONNECT
                putExtra(XBoardVpnService.EXTRA_CONFIG_PATH, configFile.absolutePath)
                putExtra(XBoardVpnService.EXTRA_NODE_NAME, node.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            connectionPrefs.setLastConnected(true)
            // Keep PREPARING until service/tun logs prove success; avoid fake CONNECTED UI.
            _state.value = VpnConnectionState.PREPARING
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

    fun syncStateFromLogs() {
        val logs = latestLogs().orEmpty()
        val latestLine = logs.lineSequence().filter { it.isNotBlank() }.lastOrNull().orEmpty()
        _state.value = when {
            logs.contains("tun established") -> VpnConnectionState.CONNECTED
            latestLine.contains("service start failed", ignoreCase = true) ||
                latestLine.contains("vpn start failed", ignoreCase = true) ||
                latestLine.contains("failed", ignoreCase = true) ||
                latestLine.contains("error", ignoreCase = true) ||
                latestLine.contains("失败") -> VpnConnectionState.ERROR
            logs.contains("prepare runtime") ||
                logs.contains("newService") ||
                logs.contains("service started") ||
                logs.contains("openTun") -> VpnConnectionState.PREPARING
            else -> _state.value
        }
    }

    suspend fun refreshSelectedNodeName(): String? = nodeRepository.currentSelectedNode()?.name

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
        proxyRuntimeManager.stop()
        _state.value = VpnConnectionState.STOPPED
    }

    fun latestLogLine(): String? = latestLogs()?.lineSequence()?.filter { it.isNotBlank() }?.lastOrNull()
}
