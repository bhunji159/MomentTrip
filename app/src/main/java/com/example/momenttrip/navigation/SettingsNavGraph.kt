package com.example.momenttrip.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.momenttrip.ui.screen.settings.AccountSettingsScreen
import com.example.momenttrip.ui.screen.settings.SettingsScreen

@Composable
fun SettingsNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(navController = navController, startDestination = "settings_main") {
        composable("settings_main") {
            SettingsScreen(
                onLogout = onLogout,
                onAccountClick = {
                    navController.navigate("account_settings")
                }
            )
        }
        composable("account_settings") {
            AccountSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
