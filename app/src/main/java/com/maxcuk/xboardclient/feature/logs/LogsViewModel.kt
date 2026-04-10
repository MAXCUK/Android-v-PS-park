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
        val runtime = vpnController.runtimeStatus()
        val diagnostic = buildString {
            appendLine("运行时状态：${runtime.message}")
            appendLine("运行库路径：${runtime.binaryPath.ifBlank { "--" }}")
            appendLine("配置文件：${runtime.configPath}")
            appendLine("日志文件：${runtime.logPath}")
            appendLine()
            append(vpnController.latestLogs() ?: "暂无运行日志")
        }
        _uiState.value = LogsUiState(diagnostic)
    }
}
