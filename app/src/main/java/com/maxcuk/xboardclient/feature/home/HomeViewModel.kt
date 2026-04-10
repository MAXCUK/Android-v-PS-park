package com.maxcuk.xboardclient.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.network.NetworkFactory
import com.maxcuk.xboardclient.core.network.XBoardRemoteDataSource
import com.maxcuk.xboardclient.core.network.model.UserInfoResponse
import com.maxcuk.xboardclient.core.repository.AuthRepository
import com.maxcuk.xboardclient.core.repository.NodeRepository
import com.maxcuk.xboardclient.core.vpn.VpnConnectionState
import com.maxcuk.xboardclient.core.vpn.VpnController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userInfo: UserInfoResponse? = null,
    val selectedNodeName: String? = null,
    val nodeCount: Int = 0,
    val vpnState: VpnConnectionState = VpnConnectionState.DISCONNECTED,
    val needsVpnPermission: Boolean = false,
    val autoReconnect: Boolean = true,
    val runtimeStatus: String = "未检测运行时",
    val runtimeBinaryPath: String = "",
    val runtimeLogPath: String = "",
    val latestLogLine: String? = null,
    val error: String? = null,
    val needsLogin: Boolean = false
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val nodeRepository: NodeRepository,
    private val vpnController: VpnController
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val runtime = vpnController.runtimeStatus()
            runCatching {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, needsLogin = false)
                val session = authRepository.currentSession() ?: error("未登录")
                val remote = XBoardRemoteDataSource(NetworkFactory.create(session.baseUrl), session.baseUrl)
                val userInfo = remote.getUserInfo(session.authToken)
                val servers = remote.fetchServers(session.authToken)
                nodeRepository.replaceNodes(servers)
                val selected = nodeRepository.currentSelectedNode()
                Triple(userInfo, selected?.name, servers.size)
            }.onSuccess { (userInfo, selectedName, nodeCount) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userInfo = userInfo,
                    selectedNodeName = selectedName,
                    nodeCount = nodeCount,
                    vpnState = vpnController.state.value,
                    autoReconnect = true,
                    runtimeStatus = runtime.message,
                    runtimeBinaryPath = runtime.binaryPath,
                    runtimeLogPath = runtime.logPath,
                    latestLogLine = vpnController.latestLogs()?.lineSequence()?.lastOrNull(),
                    error = null,
                    needsLogin = false
                )
            }.onFailure {
                val message = it.message ?: "加载失败"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = if (message == "未登录") "当前登录态已失效，请重新登录同步" else message,
                    needsLogin = message == "未登录",
                    runtimeStatus = runtime.message,
                    runtimeBinaryPath = runtime.binaryPath,
                    runtimeLogPath = runtime.logPath,
                    latestLogLine = vpnController.latestLogs()?.lineSequence()?.lastOrNull()
                )
            }
        }
    }

    fun requestConnect() { _uiState.value = _uiState.value.copy(needsVpnPermission = true, error = null) }
    fun onVpnPermissionHandled() { _uiState.value = _uiState.value.copy(needsVpnPermission = false) }

    fun connectOrDisconnect() {
        viewModelScope.launch {
            when (vpnController.state.value) {
                VpnConnectionState.CONNECTED, VpnConnectionState.PREPARING -> vpnController.disconnect()
                else -> {
                    val result = vpnController.connectSelectedNode()
                    if (result.isFailure) _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
                }
            }
            val runtime = vpnController.runtimeStatus()
            _uiState.value = _uiState.value.copy(
                vpnState = vpnController.state.value,
                runtimeStatus = runtime.message,
                runtimeBinaryPath = runtime.binaryPath,
                runtimeLogPath = runtime.logPath,
                latestLogLine = vpnController.latestLogs()?.lineSequence()?.lastOrNull()
            )
        }
    }

    fun setAutoReconnect(enabled: Boolean) {
        viewModelScope.launch {
            vpnController.setAutoReconnect(enabled)
            _uiState.value = _uiState.value.copy(autoReconnect = enabled)
        }
    }

    fun tryAutoReconnect() {
        viewModelScope.launch {
            vpnController.tryAutoReconnect()
            _uiState.value = _uiState.value.copy(vpnState = vpnController.state.value)
        }
    }
}
