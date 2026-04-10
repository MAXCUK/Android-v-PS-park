package com.maxcuk.xboardclient.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("账", color = Color(0xFF6B7280), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(uiState.session?.email ?: "未登录账户", style = MaterialTheme.typography.titleLarge, color = Color.Black, fontWeight = FontWeight.Bold)
                    Text("ID: ${uiState.session?.id ?: "--"}", color = Color(0xFF111827), style = MaterialTheme.typography.titleMedium)
                    Text("本地登录态已保存", color = Color(0xFF6B7280))
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF))
            }
        }

        MenuItem(icon = { Icon(Icons.Filled.Public, contentDescription = null, tint = Color(0xFF22C55E)) }, title = "变更国家和地区", subtitle = "切换线路与节点")
        MenuItem(icon = { Text("🎁") }, title = "推荐有奖", subtitle = "邀请好友获取奖励")
        MenuItem(icon = { Text("🔥") }, title = "大家都在玩", subtitle = "热门线路与推荐")
        MenuItem(icon = { Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF3B82F6)) }, title = "软件防丢失", subtitle = "保护安装与配置")
        MenuItem(icon = { Icon(Icons.Filled.HeadsetMic, contentDescription = null, tint = Color(0xFFA855F7)) }, title = "在线客服", subtitle = "获取帮助与反馈")
        MenuItem(icon = { Icon(Icons.Filled.Upload, contentDescription = null, tint = Color(0xFF60A5FA)) }, title = "上传日志", subtitle = "排查连接与同步问题")

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("续费会员")
        }

        Text("当前版本：测试 UI 分支", color = Color(0xFF94A3B8), modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun MenuItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(modifier = Modifier.padding(4.dp), contentAlignment = Alignment.Center) { icon() }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color.Black, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF))
        }
    }
}
