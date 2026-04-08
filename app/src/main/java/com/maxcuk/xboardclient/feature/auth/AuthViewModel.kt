package com.maxcuk.xboardclient.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.network.NetworkFactory
import com.maxcuk.xboardclient.core.network.XBoardRemoteDataSource
import com.maxcuk.xboardclient.core.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val siteName: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun preloadSite(baseUrl: String) {
        viewModelScope.launch {
            runCatching {
                val remote = XBoardRemoteDataSource(NetworkFactory.create(baseUrl))
                remote.fetchGuestConfig()?.app_name
            }.onSuccess {
                _uiState.value = _uiState.value.copy(siteName = it)
            }
        }
    }

    fun login(baseUrl: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                _uiState.value = AuthUiState(isLoading = true, siteName = _uiState.value.siteName)
                val normalizedBaseUrl = NetworkFactory.normalizeBaseUrl(baseUrl)
                val remote = XBoardRemoteDataSource(NetworkFactory.create(normalizedBaseUrl), normalizedBaseUrl)
                val token = remote.login(email, password)
                authRepository.saveSession(normalizedBaseUrl, email, token)
                val servers = remote.fetchServers(token)
                nodeRepository.replaceNodes(servers)
                refreshScheduler(true)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                onSuccess()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message ?: "登录失败")
            }
        }
    }
}
