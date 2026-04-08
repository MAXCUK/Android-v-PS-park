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
    var baseUrl by remember { mutableStateOf(defaultBaseUrl) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(baseUrl) {
        if (baseUrl.isNotBlank()) viewModel.preloadSite(baseUrl)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = uiState.siteName ?: "登录 XBoard")
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("面板地址") }
        )
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
            enabled = baseUrl.isNotBlank() && email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("登录并同步节点")
            }
        }
    }
}
