package com.maxcuk.xboardclient.feature.nodeDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
    val node = uiState.node

    LaunchedEffect(nodeId) { viewModel.load(nodeId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("节点详情")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("名称：${node?.name ?: "--"}")
                Text("协议：${node?.type ?: "--"}")
                Text("地址：${displayHost(node?.host)}")
                Text("端口：${displayPort(node?.port)}")
                Text("UUID：${node?.uuid ?: "--"}")
                Text("密码：${node?.password ?: "--"}")
                Text("加密：${node?.method ?: "--"}")
                Text("安全：${node?.security ?: "--"}")
                Text("SNI：${node?.sni ?: "--"}")
                Text("传输：${node?.network ?: "--"}")
                Text("Path：${node?.path ?: "--"}")
                Text("Host：${node?.hostHeader ?: "--"}")
                Text("Service Name：${node?.serviceName ?: "--"}")
                Text("Public Key：${node?.publicKey ?: "--"}")
                Text("Short ID：${node?.shortId ?: "--"}")
            }
        }
        uiState.message?.let { Text(it) }
        Button(onClick = { viewModel.select(nodeId) }, modifier = Modifier.fillMaxWidth()) {
            Text("设为当前节点")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("返回")
        }
    }
}

private fun displayHost(value: String?): String = value?.takeIf { it.isNotBlank() } ?: "--"
private fun displayPort(value: Int?): String = value?.takeIf { it > 0 }?.toString() ?: "--"
