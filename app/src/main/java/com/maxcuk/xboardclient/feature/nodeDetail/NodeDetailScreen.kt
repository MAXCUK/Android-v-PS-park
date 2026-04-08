package com.maxcuk.xboardclient.feature.nodeDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NodeDetailScreen(
    nodeId: String,
    viewModel: NodeDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(nodeId) { viewModel.load(nodeId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("节点详情")
        Text("名称：${uiState.node?.name ?: "--"}")
        Text("协议：${uiState.node?.type ?: "--"}")
        Text("地址：${uiState.node?.host ?: "--"}:${uiState.node?.port ?: 0}")
        Text("UUID：${uiState.node?.uuid ?: "--"}")
        Text("SNI：${uiState.node?.sni ?: "--"}")
        Text("传输：${uiState.node?.network ?: "--"}")
        Text("Path：${uiState.node?.path ?: "--"}")
        uiState.message?.let { Text(it) }
        Button(onClick = { viewModel.select(nodeId) }, modifier = Modifier.fillMaxWidth()) {
            Text("设为当前节点")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回")
        }
    }
}
