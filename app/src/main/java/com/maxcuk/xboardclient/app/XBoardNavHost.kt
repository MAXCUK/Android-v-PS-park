package com.maxcuk.xboardclient.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maxcuk.xboardclient.feature.auth.AuthScreen
import com.maxcuk.xboardclient.feature.auth.AuthViewModel
import com.maxcuk.xboardclient.feature.home.HomeScreen
import com.maxcuk.xboardclient.feature.home.HomeViewModel
import com.maxcuk.xboardclient.feature.logs.LogsScreen
import com.maxcuk.xboardclient.feature.logs.LogsViewModel
import com.maxcuk.xboardclient.feature.nodeDetail.NodeDetailScreen
import com.maxcuk.xboardclient.feature.nodeDetail.NodeDetailViewModel
import com.maxcuk.xboardclient.feature.nodes.NodesScreen
import com.maxcuk.xboardclient.feature.nodes.NodesViewModel
import com.maxcuk.xboardclient.feature.profile.ProfileScreen
import com.maxcuk.xboardclient.feature.profile.ProfileViewModel
import com.maxcuk.xboardclient.feature.settings.SettingsScreen
import com.maxcuk.xboardclient.feature.settings.SettingsViewModel

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val NODES = "nodes"
    const val PROFILE = "profile"
    const val LOGS = "logs"
    const val SETTINGS = "settings"
    const val NODE_DETAIL = "node_detail"
}

@Composable
fun XBoardNavHost(
    navController: NavHostController,
    container: AppContainer,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val authViewModel = remember {
        AuthViewModel(
            container.authRepository,
            container.nodeRepository,
            container::setRefreshEnabled
        )
    }
    val homeViewModel = remember { HomeViewModel(container.authRepository, container.nodeRepository, container.vpnController) }
    val nodesViewModel = remember { NodesViewModel(container.nodeRepository, container.authRepository) }
    val profileViewModel = remember { ProfileViewModel(container.authRepository) }
    val logsViewModel = remember { LogsViewModel(container.vpnController) }
    val nodeDetailViewModel = remember { NodeDetailViewModel(container.nodeRepository) }
    val settingsViewModel = remember { SettingsViewModel(container.vpnController, container.connectionPrefs) }

    NavHost(navController = navController, startDestination = Routes.AUTH) {
        composable(Routes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                defaultBaseUrl = "https://ax.ty666.help",
                onLoginClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenNodes = { navController.navigate(Routes.NODES) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenLogs = { navController.navigate(Routes.LOGS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenLogin = {
                    authViewModel.disableAutoRestore()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.NODES) {
            NodesScreen(
                viewModel = nodesViewModel,
                onOpenNode = { nodeId -> navController.navigate("${Routes.NODE_DETAIL}/$nodeId") },
                onSelectNode = { nodeId -> nodesViewModel.selectNode(nodeId) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("${Routes.NODE_DETAIL}/{nodeId}", arguments = listOf(navArgument("nodeId") { defaultValue = "" })) { backStackEntry ->
            NodeDetailScreen(
                nodeId = backStackEntry.arguments?.getString("nodeId").orEmpty(),
                viewModel = nodeDetailViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(viewModel = profileViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.LOGS) {
            LogsScreen(viewModel = logsViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = settingsViewModel, onBack = { navController.popBackStack() })
        }
    }
}
