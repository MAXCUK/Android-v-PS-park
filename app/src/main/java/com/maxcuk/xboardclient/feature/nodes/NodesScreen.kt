package com.maxcuk.xboardclient.feature.nodes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NodesScreen(
    viewModel: NodesViewModel,
    onOpenNode: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "节点列表")
        Text(text = "返回", modifier = Modifier.clickable { onBack() })
        if (uiState.nodes.isEmpty()) {
            Text(text = "暂无节点，请先登录同步或下拉刷新后再试")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.nodes) { node ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNode(node.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = node.name)
                            Text(text = "协议：${node.type}")
                            Text(text = "地址：${node.host}:${node.port}")
                            Text(text = if (node.isSelected) "已选中" else "点击查看详情")
                        }
                    }
                }
            }
        }
    }
}
