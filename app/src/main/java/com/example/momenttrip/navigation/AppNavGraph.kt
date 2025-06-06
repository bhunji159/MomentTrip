package com.example.momenttrip.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.momenttrip.ui.screen.main.MainScreen
import com.example.momenttrip.ui.screen.addSchedule.AddScheduleScreen
import com.example.momenttrip.viewmodel.TripViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    centerTab: String,
    onLogout: () -> Unit,
    tripViewModel: TripViewModel
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
                tripViewModel = tripViewModel
            )
        }

        composable("addSchedule") {
            AddScheduleScreen(
                onBack = { navController.popBackStack() },
                tripViewModel = tripViewModel
            )
        }
    }
}
