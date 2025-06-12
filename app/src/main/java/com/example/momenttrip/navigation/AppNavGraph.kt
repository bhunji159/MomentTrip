package com.example.momenttrip.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.momenttrip.data.SchedulePlan
import com.example.momenttrip.repository.TripRepository
import com.example.momenttrip.ui.screen.ChecklistScreen
import com.example.momenttrip.ui.screen.main.TripListScreen
import com.example.momenttrip.ui.screen.addSchedule.AddScheduleScreen
import com.example.momenttrip.ui.screen.main.AddTripScreen
import com.example.momenttrip.ui.screen.main.MainScreen
import com.example.momenttrip.ui.screen.main.MapScreen
import com.example.momenttrip.viewmodel.TripViewModel
import com.example.momenttrip.viewmodel.UserViewModel
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    centerTab: String,
    onLogout: () -> Unit,
    tripViewModel: TripViewModel,
    userViewModel: UserViewModel
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "start" else "login"
    ) {
        composable("start") {
            val user by userViewModel.user.collectAsState()
            var isLoading by remember { mutableStateOf(true) }
            var isTripOngoing by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(user) {
                if (user == null) {
                    userViewModel.loadCurrentUser()
                } else {
                    try {
                        val tripId = user?.current_trip_id
                        if (tripId != null) {
                            val trip = TripRepository.getTripById(tripId)
                            if (trip != null) {
                                val start = trip.start_date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                                val end = trip.end_date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                                val today = LocalDate.now()

                                isTripOngoing = if (start != null && end != null) {
                                    !today.isBefore(start) && !today.isAfter(end)
                                } else {
                                    false
                                }
                            } else {
                                isTripOngoing = false
                            }
                        } else {
                            isTripOngoing = false
                        }
                    } catch (e: Exception) {
                        Log.e("StartScreenSelector", "Error checking trip status", e)
                        isTripOngoing = false
                    }
                    isLoading = false
                }
            }

            if (isLoading || user == null) {
                CircularProgressIndicator()
            } else {
                if (isTripOngoing == true) {
                    val trip = runBlocking { TripRepository.getTripById(user!!.current_trip_id!!) }
                    MainScreen(
                        trip = trip,
                        onLogout = onLogout,
                        centerTab = centerTab,
                        onAddScheduleClick = { navController.navigate("addSchedule") },
                        onEditScheduleClick = { plan -> navController.navigate("addSchedule/${plan.documentId}") },
                        onMapClick = { navController.navigate("map") },
                        tripViewModel = tripViewModel,
                        navController = navController,
                        userUid = trip?.owner_uid ?: ""
                    )
                } else {
                    TripListScreen(
                        navController = navController,
                        userUid = user!!.uid
                    )
                }
            }
        }

        composable("main") {
            val user by userViewModel.user.collectAsState()

            user?.current_trip_id?.let { tripId ->
                val trip = runBlocking { TripRepository.getTripById(tripId) }
                MainScreen(
                    trip = trip,
                    onLogout = onLogout,
                    centerTab = centerTab,
                    onAddScheduleClick = {
                        navController.navigate("addSchedule")
                    },
                    onEditScheduleClick = { plan: SchedulePlan ->
                        navController.navigate("addSchedule/${plan.documentId}")
                    },
                    onMapClick = {
                        navController.navigate("map")
                    },
                    tripViewModel = tripViewModel,
                    navController = navController,
                    userUid = trip?.owner_uid ?: ""
                )
            } ?: run {
                CircularProgressIndicator()
            }
        }

        composable("addTrip") {
            AddTripScreen(
                viewModel = tripViewModel,
                onTripCreated = { tripId ->
                    val encodedTripId = Uri.encode(tripId)
                    navController.navigate("tripMain/$encodedTripId") {
                        popUpTo("start") { inclusive = false }
                    }
                }
            )
        }

        composable("addSchedule") {
            AddScheduleScreen(
                onBack = { navController.popBackStack() },
                tripViewModel = tripViewModel,
                planId = null
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
                planId = planId
            )
        }

        composable("map") {
            MapScreen(navController = navController)
        }

        composable(
            "tripMain/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            val trip = runBlocking { TripRepository.getTripById(tripId) }

            MainScreen(
                trip = trip,
                onLogout = onLogout,
                centerTab = "currentTrip",
                onAddScheduleClick = {
                    navController.navigate("addSchedule")
                },
                onEditScheduleClick = { plan ->
                    navController.navigate("addSchedule/${plan.documentId}")
                },
                onMapClick = {
                    navController.navigate("map")
                },
                tripViewModel = tripViewModel,
                navController = navController,
                userUid = trip?.owner_uid ?: ""
            )
        }

        composable(
            route = "checklist/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            ChecklistScreen(
                tripId = tripId,
                onBack = {
                    navController.popBackStack("start", inclusive = false)
                }
            )
        }
    }
}