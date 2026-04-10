package com.maxcuk.xboardclient.feature.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    val mainButtonText = if (uiState.vpnState.name == "CONNECTED") "断开快连" else "开启快连"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFFFF6A88), Color(0xFF5CC7FF))))
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "菜单", tint = Color.White, modifier = Modifier.clickable { onOpenSettings() })
            Text("星隧互联", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("账户", color = Color.White, modifier = Modifier.clickable { onOpenProfile() })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAF4FF))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("网络：${uiState.selectedNodeName ?: "自动"}", color = Color(0xFF1F2937), style = MaterialTheme.typography.titleMedium)
                Text("状态：$statusText", color = Color(0xFF5B6475))
            }
            Text("推荐有奖", color = Color(0xFF6B46FF), fontWeight = FontWeight.SemiBold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC5D4))
                )
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF97AF))
                )
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5C86))
                        .clickable {
                            if (uiState.vpnState.name == "CONNECTED") viewModel.connectOrDisconnect() else viewModel.requestConnect()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("∞", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Text(mainButtonText, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCardLight("节点数量", uiState.nodeCount.toString(), Modifier.weight(1f))
                MetricCardLight("套餐流量", formatBytes(uiState.userInfo?.transfer_enable), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCardLight("已用上行", formatBytes(uiState.userInfo?.u), Modifier.weight(1f))
                MetricCardLight("已用下行", formatBytes(uiState.userInfo?.d), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("账号邮箱：${uiState.userInfo?.email ?: uiState.sessionEmail ?: "--"}", color = Color(0xFF111827))
                    Text("到期时间：${formatExpiry(uiState.userInfo?.expired_at)}", color = Color(0xFF6B7280))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("自动重连", color = Color(0xFF111827))
                        Switch(checked = uiState.autoReconnect, onCheckedChange = viewModel::setAutoReconnect)
                    }
                    uiState.error?.takeIf { it.isNotBlank() }?.let {
                        Text("提示：$it", color = Color(0xFFE67E22))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onOpenNodes, modifier = Modifier.weight(1f)) { Text("选择节点") }
                        Button(onClick = onOpenLogs, modifier = Modifier.weight(1f)) { Text("运行日志") }
                    }
                    Button(onClick = {
                        if (uiState.userInfo == null) onOpenLogin() else viewModel.refresh()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (uiState.userInfo == null) "重新登录" else "刷新账户与节点")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(18.dp)
                ) {
                    Text("尊享会员", color = Color(0xFF111827), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${formatExpiry(uiState.userInfo?.expired_at)} 到期", color = Color(0xFFEF4444))
                }
                Row(
                    modifier = Modifier
                        .background(Color(0xFF2F80ED))
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                        .clickable { onOpenProfile() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "续费", tint = Color.White)
                    Text("续费", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("当前版本：0.1.3", color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MetricCardLight(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color(0xFF94A3B8))
            Text(value, color = Color(0xFF111827), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
