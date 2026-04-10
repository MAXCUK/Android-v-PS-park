package com.maxcuk.xboardclient.feature.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maxcuk.xboardclient.core.vpn.VpnPermissionHelper

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenNodes: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vpnPermissionLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        viewModel.onVpnPermissionHandled()
        if (result.resultCode == Activity.RESULT_OK) viewModel.connectOrDisconnect()
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        viewModel.tryAutoReconnect()
    }

    LaunchedEffect(uiState.needsVpnPermission) {
        if (uiState.needsVpnPermission) {
            val intent = VpnPermissionHelper.prepare(context)
            if (intent != null) vpnPermissionLauncher.launch(intent) else {
                viewModel.onVpnPermissionHandled()
                viewModel.connectOrDisconnect()
            }
        }
    }

    LaunchedEffect(uiState.needsLogin) {
        if (uiState.needsLogin) onOpenLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("连接状态：${uiState.vpnState}")
                Text("当前节点：${uiState.selectedNodeName ?: "未选择"}")
                Text("邮箱：${uiState.userInfo?.email ?: "--"}")
                Text("流量上限：${uiState.userInfo?.transfer_enable ?: 0}")
                Text("已用上行：${uiState.userInfo?.u ?: 0}")
                Text("已用下行：${uiState.userInfo?.d ?: 0}")
                Text("节点数量：${uiState.nodeCount}")
                Text("运行时：${uiState.runtimeStatus}")
                Text("最新错误：${uiState.error ?: "--"}")
                Text("运行库：${uiState.runtimeBinaryPath}")
                Text("日志文件：${uiState.runtimeLogPath}")
                Text("最新日志：${uiState.latestLogLine ?: "--"}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("自动重连")
                    Switch(checked = uiState.autoReconnect, onCheckedChange = viewModel::setAutoReconnect)
                }
                Button(onClick = {
                    if (uiState.vpnState.name == "CONNECTED") viewModel.connectOrDisconnect() else viewModel.requestConnect()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.vpnState.name == "CONNECTED") "断开连接" else "一键连接")
                }
                Button(onClick = { viewModel.refresh() }, modifier = Modifier.fillMaxWidth()) { Text("从面板刷新") }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onOpenNodes, modifier = Modifier.fillMaxWidth()) { Text("节点列表") }
            Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) { Text("账户信息") }
            Button(onClick = onOpenLogs, modifier = Modifier.fillMaxWidth()) { Text("运行日志") }
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) { Text("设置") }
        }
    }
}
