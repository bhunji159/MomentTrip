package com.example.momenttrip.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.momenttrip.ui.screen.ChecklistScreen
import com.example.momenttrip.ui.screen.SettingsScreen
import com.example.momenttrip.ui.screen.TripListScreen
import com.example.momenttrip.ui.screen.addSchedule.AddScheduleScreen
import com.example.momenttrip.ui.screen.main.MainScreen
import com.example.momenttrip.ui.screen.settings.AccountSettingsScreen
import com.example.momenttrip.viewmodel.SettingsViewModel
import com.example.momenttrip.viewmodel.TripViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    centerTab: String,
    onLogout: () -> Unit,
    tripViewModel: TripViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        composable("main") {
            MainScreen(
                onLogout = onLogout,
                centerTab = centerTab,
                onAddScheduleClick = {
                    navController.navigate("addSchedule")
                },
                tripViewModel = tripViewModel,
                settingsViewModel = settingsViewModel
            )
        }

        composable("addSchedule") {
            AddScheduleScreen(
                onBack = { navController.popBackStack() },
                tripViewModel = tripViewModel
            )
        }

        composable("triplist/{userUid}") { backStackEntry ->
            val userUid = backStackEntry.arguments?.getString("userUid") ?: return@composable
            TripListScreen(
                navController = navController,
                userUid = "Drn8N6omyiTy91sb7z0c7482bjH3"
            )
        }

        composable("checklist/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            ChecklistScreen(tripId = tripId)
        }

        composable("Settings") {
            SettingsScreen(
                viewModel = viewModel(),
                onNavigateToProfile = {
                    navController.navigate("AccountSettings")
                },
                onNavigateToTerms = {
                    navController.navigate("Terms")
                },
                onLogout = onLogout
            )
        }

        composable("AccountSettings") {
            AccountSettingsScreen()
        }

    }
}