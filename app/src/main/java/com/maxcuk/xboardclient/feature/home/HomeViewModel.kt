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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userInfo: UserInfoResponse? = null,
    val sessionEmail: String? = null,
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

    init {
        nodeRepository.observeNodes()
            .onEach { nodes ->
                val selected = nodes.firstOrNull { it.isSelected }
                _uiState.value = _uiState.value.copy(
                    selectedNodeName = selected?.name,
                    nodeCount = nodes.size
                )
            }
            .launchIn(viewModelScope)

        vpnController.state
            .onEach { state ->
                _uiState.value = _uiState.value.copy(vpnState = state)
            }
            .launchIn(viewModelScope)
    }

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
                HomeUiState(
                    isLoading = false,
                    userInfo = userInfo,
                    sessionEmail = session.email,
                    selectedNodeName = selected?.name ?: session.email,
                    nodeCount = servers.size,
                    vpnState = vpnController.state.value,
                    autoReconnect = true,
                    runtimeStatus = runtime.message,
                    runtimeBinaryPath = runtime.binaryPath,
                    runtimeLogPath = runtime.logPath,
                    latestLogLine = vpnController.latestLogLine(),
                    error = null,
                    needsLogin = false
                )
            }.onSuccess {
                _uiState.value = it
            }.onFailure {
                val message = (it.message ?: "加载失败").ifBlank { "加载失败" }
                val needsRelogin = message == "未登录" || message.contains("重新登录") || message.contains("401") || message.contains("403") || message.contains("邮箱或密码错误")
                if (needsRelogin) {
                    viewModelScope.launch { authRepository.clearSession() }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = if (needsRelogin) "登录态已失效，请重新登录" else "加载失败：$message",
                    needsLogin = needsRelogin,
                    runtimeStatus = runtime.message,
                    runtimeBinaryPath = runtime.binaryPath,
                    runtimeLogPath = runtime.logPath,
                    latestLogLine = vpnController.latestLogLine()
                )
            }
        }
    }

    fun requestConnect() {
        _uiState.value = _uiState.value.copy(needsVpnPermission = true, error = null)
    }

    fun onVpnPermissionHandled() {
        _uiState.value = _uiState.value.copy(needsVpnPermission = false)
    }

    fun connectOrDisconnect() {
        viewModelScope.launch {
            when (vpnController.state.value) {
                VpnConnectionState.CONNECTED, VpnConnectionState.PREPARING -> vpnController.disconnect()
                else -> {
                    val result = vpnController.connectSelectedNode()
                    if (result.isFailure) {
                        _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
                    } else {
                        repeat(8) {
                            delay(500)
                            vpnController.syncStateFromLogs()
                            val runtime = vpnController.runtimeStatus()
                            _uiState.value = _uiState.value.copy(
                                vpnState = vpnController.state.value,
                                runtimeStatus = runtime.message,
                                runtimeBinaryPath = runtime.binaryPath,
                                runtimeLogPath = runtime.logPath,
                                latestLogLine = vpnController.latestLogLine()
                            )
                            if (vpnController.state.value == VpnConnectionState.CONNECTED || vpnController.state.value == VpnConnectionState.ERROR) return@repeat
                        }
                    }
                }
            }
            vpnController.syncStateFromLogs()
            val runtime = vpnController.runtimeStatus()
            _uiState.value = _uiState.value.copy(
                vpnState = vpnController.state.value,
                runtimeStatus = runtime.message,
                runtimeBinaryPath = runtime.binaryPath,
                runtimeLogPath = runtime.logPath,
                latestLogLine = vpnController.latestLogLine()
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
            _uiState.value = _uiState.value.copy(
                vpnState = vpnController.state.value,
                latestLogLine = vpnController.latestLogLine()
            )
        }
    }
}
