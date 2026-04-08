package com.maxcuk.xboardclient.feature.logs

import androidx.lifecycle.ViewModel
import com.maxcuk.xboardclient.core.vpn.VpnController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LogsUiState(
    val content: String = "暂无日志"
)

class LogsViewModel(
    private val vpnController: VpnController
) : ViewModel() {
    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = LogsUiState(vpnController.latestLogs() ?: "暂无日志")
    }
}
