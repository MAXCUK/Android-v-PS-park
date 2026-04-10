package com.maxcuk.xboardclient.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    defaultBaseUrl: String,
    onLoginClick: () -> Unit
) {
    val baseUrl = defaultBaseUrl
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (baseUrl.isNotBlank()) viewModel.preloadSite(baseUrl)
    }

    LaunchedEffect(uiState.isSuccess, uiState.hasLocalSession, uiState.restoringSession) {
        if ((uiState.isSuccess || uiState.hasLocalSession) && !uiState.restoringSession) onLoginClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = uiState.siteName ?: "登录 XBoard")

        if (uiState.restoringSession) {
            Text(text = "正在恢复本地登录态…")
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("邮箱") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码") }
            )
            uiState.error?.let { Text(text = it) }
            Button(
                onClick = { viewModel.login(baseUrl, email, password, onLoginClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("登录并同步节点")
                }
            }
        }
    }
}
