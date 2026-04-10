package com.maxcuk.xboardclient.feature.nodeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.repository.NodeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NodeDetailUiState(
    val node: NodeEntity? = null,
    val message: String? = null
)

class NodeDetailViewModel(
    private val nodeRepository: NodeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NodeDetailUiState())
    val uiState: StateFlow<NodeDetailUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun load(nodeId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            nodeRepository.observeNodes().collect { list ->
                _uiState.value = _uiState.value.copy(node = list.firstOrNull { it.id == nodeId })
            }
        }
    }

    fun select(nodeId: String) {
        viewModelScope.launch {
            nodeRepository.selectNode(nodeId)
            val selected = nodeRepository.currentSelectedNode()
            _uiState.value = _uiState.value.copy(node = selected, message = "已设为当前节点")
        }
    }
}
