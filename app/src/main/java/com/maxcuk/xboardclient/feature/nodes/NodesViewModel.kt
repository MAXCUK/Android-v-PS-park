package com.maxcuk.xboardclient.feature.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.repository.NodeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NodesUiState(
    val nodes: List<NodeEntity> = emptyList(),
    val selectedNodeId: String? = null
)

class NodesViewModel(
    private val nodeRepository: NodeRepository
) : ViewModel() {
    val uiState: StateFlow<NodesUiState> = nodeRepository.observeNodes()
        .map { nodes ->
            NodesUiState(
                nodes = nodes,
                selectedNodeId = nodes.firstOrNull { it.isSelected }?.id
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NodesUiState())

    fun selectNode(nodeId: String) {
        viewModelScope.launch {
            nodeRepository.selectNode(nodeId)
        }
    }
}
