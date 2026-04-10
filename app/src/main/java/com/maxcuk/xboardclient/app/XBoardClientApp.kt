package com.maxcuk.xboardclient.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class BottomTab(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun XBoardClientApp(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val tabs = listOf(
        BottomTab(Routes.HOME, "首页") { Icon(Icons.Filled.Home, contentDescription = "首页") },
        BottomTab(Routes.NODES, "节点") { Icon(Icons.Filled.List, contentDescription = "节点") },
        BottomTab(Routes.PROFILE, "账户") { Icon(Icons.Filled.Person, contentDescription = "账户") },
        BottomTab(Routes.SETTINGS, "设置") { Icon(Icons.Filled.Settings, contentDescription = "设置") }
    )
    val showBottomBar = currentDestination?.route in setOf(Routes.HOME, Routes.NODES, Routes.PROFILE, Routes.SETTINGS)

    MaterialTheme {
        Surface {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar {
                            tabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = tab.icon,
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                XBoardNavHost(navController = navController, container = container, paddingValues = paddingValues)
            }
        }
    }
}
