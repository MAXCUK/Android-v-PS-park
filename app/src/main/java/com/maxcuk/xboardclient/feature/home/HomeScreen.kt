package com.maxcuk.xboardclient.feature.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.maxcuk.xboardclient.core.vpn.VpnPermissionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val statusText = when (uiState.vpnState.name) {
        "CONNECTED" -> "已连接"
        "PREPARING" -> "连接中"
        "ERROR" -> "连接异常"
        else -> "未连接"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101114), Color(0xFF181B22), Color(0xFF111318))
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1F2530), Color(0xFF161B23))))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("星隧互联", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("连接状态：$statusText", color = Color(0xFFB8C0CC))
                Text(
                    "当前节点：${uiState.selectedNodeName ?: if (uiState.nodeCount > 0) "请选择节点" else "暂无节点"}",
                    color = Color.White
                )
                Text("账号邮箱：${uiState.userInfo?.email ?: uiState.sessionEmail ?: "--"}", color = Color(0xFFB8C0CC))
                uiState.error?.takeIf { it.isNotBlank() }?.let {
                    Text("提示：$it", color = Color(0xFFFFB86B))
                }
                Button(onClick = {
                    if (uiState.vpnState.name == "CONNECTED") viewModel.connectOrDisconnect() else viewModel.requestConnect()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.vpnState.name == "CONNECTED") "断开连接" else "一键连接")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("节点数量", uiState.nodeCount.toString(), Modifier.weight(1f))
            MetricCard("套餐流量", formatBytes(uiState.userInfo?.transfer_enable), Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("已用上行", formatBytes(uiState.userInfo?.u), Modifier.weight(1f))
            MetricCard("已用下行", formatBytes(uiState.userInfo?.d), Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("到期时间：${formatExpiry(uiState.userInfo?.expired_at)}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("自动重连")
                    Switch(checked = uiState.autoReconnect, onCheckedChange = viewModel::setAutoReconnect)
                }
                Button(onClick = onOpenNodes, modifier = Modifier.fillMaxWidth()) { Text("选择节点") }
                Button(onClick = {
                    if (uiState.userInfo == null) onOpenLogin() else viewModel.refresh()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (uiState.userInfo == null) "重新登录" else "刷新账户与节点")
                }
                Button(onClick = onOpenLogs, modifier = Modifier.fillMaxWidth()) { Text("查看运行日志") }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color(0xFF7E8794))
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun formatBytes(value: Long?): String {
    if (value == null || value <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var size = value.toDouble()
    var index = 0
    while (size >= 1024 && index < units.lastIndex) {
        size /= 1024
        index++
    }
    return String.format(Locale.US, "%.2f %s", size, units[index])
}

private fun formatExpiry(value: Long?): String {
    if (value == null || value <= 0) return "--"
    val millis = if (value < 10_000_000_000L) value * 1000 else value
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
    }.getOrDefault("--")
}
