package com.maxcuk.xboardclient.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun XBoardClientApp(container: AppContainer) {
    val navController = rememberNavController()
    MaterialTheme {
        Surface {
            XBoardNavHost(navController = navController, container = container)
        }
    }
}
