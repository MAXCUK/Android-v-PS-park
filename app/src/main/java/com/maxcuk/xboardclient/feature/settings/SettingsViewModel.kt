package com.maxcuk.xboardclient.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.vpn.VpnController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val autoReconnect: Boolean = true,
    val message: String? = null
)

class SettingsViewModel(
    private val vpnController: VpnController
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setAutoReconnect(enabled: Boolean) {
        viewModelScope.launch {
            vpnController.setAutoReconnect(enabled)
            _uiState.value = _uiState.value.copy(autoReconnect = enabled, message = "设置已保存")
        }
    }
}
