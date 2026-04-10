package com.maxcuk.xboardclient.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.network.NetworkFactory
import com.maxcuk.xboardclient.core.network.XBoardRemoteDataSource
import com.maxcuk.xboardclient.core.repository.AuthRepository
import com.maxcuk.xboardclient.core.repository.NodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val siteName: String? = null,
    val hasLocalSession: Boolean = false,
    val restoringSession: Boolean = true
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val nodeRepository: NodeRepository,
    private val refreshScheduler: (Boolean) -> Unit
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = authRepository.currentSession()
            _uiState.value = _uiState.value.copy(
                hasLocalSession = session != null,
                restoringSession = false
            )
        }
    }

    fun disableAutoRestore() {
        _uiState.value = _uiState.value.copy(hasLocalSession = false, restoringSession = false)
    }

    fun preloadSite(baseUrl: String) {
        viewModelScope.launch {
            runCatching {
                val normalizedBaseUrl = NetworkFactory.normalizeBaseUrl(baseUrl)
                val remote = XBoardRemoteDataSource(NetworkFactory.create(normalizedBaseUrl), normalizedBaseUrl)
                remote.fetchGuestConfig()?.let { it.app_name ?: it.app_description ?: it.app_url }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(siteName = it)
            }
        }
    }

    fun login(baseUrl: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(
                isLoading = true,
                siteName = _uiState.value.siteName,
                hasLocalSession = _uiState.value.hasLocalSession,
                restoringSession = false
            )
            runCatching {
                val normalizedBaseUrl = NetworkFactory.normalizeBaseUrl(baseUrl)
                val remote = XBoardRemoteDataSource(NetworkFactory.create(normalizedBaseUrl), normalizedBaseUrl)
                val token = remote.login(email, password)
                authRepository.saveSession(normalizedBaseUrl, email, token)
                runCatching {
                    val servers = remote.fetchServers(token)
                    nodeRepository.replaceNodes(servers)
                    refreshScheduler(true)
                }.onFailure {
                    _uiState.value = _uiState.value.copy(error = "登录成功，但节点同步失败：${it.message ?: "未知错误"}")
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    hasLocalSession = true,
                    restoringSession = false
                )
                onSuccess()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "登录失败",
                    restoringSession = false
                )
            }
        }
    }
}
