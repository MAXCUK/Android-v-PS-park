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
import com.maxcuk.xboardclient.core.repository.rawFields

@Composable
fun NodeDetailScreen(
    nodeId: String,
    viewModel: NodeDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val node = uiState.node
    val raw = node?.rawFields()

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
                Text("地址：${raw?.host ?: "--"}")
                Text("端口：${raw?.port?.takeIf { it > 0 } ?: "--"}")
                Text("UUID：${raw?.uuid ?: "--"}")
                Text("密码：${raw?.password ?: "--"}")
                Text("加密：${raw?.method ?: "--"}")
                Text("安全：${raw?.security ?: "--"}")
                Text("SNI：${raw?.sni ?: "--"}")
                Text("传输：${raw?.network ?: "--"}")
                Text("Path：${raw?.path ?: "--"}")
                Text("Host：${raw?.hostHeader ?: "--"}")
                Text("Service Name：${raw?.serviceName ?: "--"}")
                Text("Public Key：${raw?.publicKey ?: "--"}")
                Text("Short ID：${raw?.shortId ?: "--"}")
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
