package com.example.momenttrip.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.momenttrip.data.SchedulePlan
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
                tripViewModel = tripViewModel,
                onEditScheduleClick = { plan: SchedulePlan ->
                    navController.navigate("addSchedule/${plan.documentId}")
                }
            )
        }
        composable("addSchedule") {
            AddScheduleScreen(
                onBack = { navController.popBackStack() },
                tripViewModel = tripViewModel,
                planId = null // 추가 모드
            )
        }

        composable(
            "addSchedule/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            AddScheduleScreen(
                onBack = { navController.popBackStack() },
                tripViewModel = tripViewModel,
                planId = planId // 이걸 이용해서 ViewModel에서 데이터 로딩
            )
        }

    }
}
