package com.maxcuk.xboardclient.feature.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.network.NetworkFactory
import com.maxcuk.xboardclient.core.network.XBoardRemoteDataSource
import com.maxcuk.xboardclient.core.repository.AuthRepository
import com.maxcuk.xboardclient.core.repository.NodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NodesUiState(
    val nodes: List<NodeEntity> = emptyList(),
    val selectedNodeId: String? = null,
    val isRefreshing: Boolean = false,
    val message: String? = null
)

class NodesViewModel(
    private val nodeRepository: NodeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val refreshState = MutableStateFlow(false)
    private val messageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NodesUiState> = combine(
        nodeRepository.observeNodes(),
        refreshState,
        messageState
    ) { nodes, refreshing, message ->
        NodesUiState(
            nodes = nodes,
            selectedNodeId = nodes.firstOrNull { it.isSelected }?.id,
            isRefreshing = refreshing,
            message = message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NodesUiState())

    fun selectNode(nodeId: String) {
        viewModelScope.launch {
            nodeRepository.selectNode(nodeId)
            messageState.value = "已切换当前节点"
        }
    }

    fun refreshFromPanel() {
        viewModelScope.launch {
            refreshState.value = true
            messageState.value = null
            runCatching {
                val session = authRepository.currentSession() ?: error("当前没有本地登录态，请先重新登录")
                val remote = XBoardRemoteDataSource(NetworkFactory.create(session.baseUrl), session.baseUrl)
                val servers = remote.fetchServers(session.authToken)
                check(servers.isNotEmpty()) { "官方面板返回 0 个节点" }
                nodeRepository.replaceNodes(servers)
                val storedNodes = nodeRepository.observeNodes().first()
                val storedCount = storedNodes.size
                check(storedCount > 0) { "同步后仍为 0 个节点：接口返回 ${servers.size} 个，但没有任何节点成功入库" }
                "节点同步完成，共 ${storedCount} 个节点"
            }.onSuccess {
                messageState.value = it
            }.onFailure {
                messageState.value = "节点同步失败：${it.message ?: "未知错误"}"
            }
            refreshState.value = false
        }
    }
}
