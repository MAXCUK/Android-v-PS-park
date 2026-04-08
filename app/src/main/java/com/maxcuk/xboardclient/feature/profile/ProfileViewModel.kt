package com.maxcuk.xboardclient.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.database.entity.SessionEntity
import com.maxcuk.xboardclient.core.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val session: SessionEntity? = null
)

class ProfileViewModel(
    authRepository: AuthRepository
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = authRepository.observeSession()
        .map { ProfileUiState(session = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}
