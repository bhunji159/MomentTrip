package com.example.momenttrip.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.momenttrip.data.SchedulePlan
import com.example.momenttrip.data.Trip
import com.example.momenttrip.navigation.SettingsNavGraph
import com.example.momenttrip.ui.screen.TripListScreen
import com.example.momenttrip.ui_screen.ExpenseMainScreen
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavHostController,
    trip: Trip?,
    onLogout: () -> Unit,
    centerTab: String,
    onAddScheduleClick: () -> Unit,
    onEditScheduleClick: (SchedulePlan) -> Unit,
    onMapClick: () -> Unit,
    tripViewModel: TripViewModel? = null,
    userUid: String
) {
    val selectedTab = remember { mutableStateOf("center") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val settingsNavController = if (selectedTab.value == "settings") {
        rememberNavController()
    } else {
        remember { mutableStateOf<NavHostController?>(null) }.value
    }

    val selectedDate = remember { trip?.start_date?.toDate()
        ?.let { mutableStateOf(it.toLocalDate()) } }

    if (selectedDate != null) {
        LaunchedEffect(selectedDate.value) {
            if (tripViewModel != null) {
                if (trip != null) {
                    tripViewModel.loadSchedulePlans(trip.trip_id, selectedDate.value)
                }
            }
        }
    }

    var schedulesForDate: List<SchedulePlan> by remember { mutableStateOf(emptyList()) }

    tripViewModel?.let { vm ->
        val observed by vm.schedulesForDate.collectAsState()
        schedulesForDate = observed
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("메뉴", modifier = Modifier.padding(16.dp))

                NavigationDrawerItem(
                    label = { Text("가계부") },
                    selected = selectedTab.value == "expense",
                    onClick = {
                        selectedTab.value = "expense"
                        coroutineScope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("일기") },
                    selected = selectedTab.value == "diary",
                    onClick = {
                        selectedTab.value = "diary"
                        coroutineScope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("알림") },
                    selected = selectedTab.value == "notification",
                    onClick = {
                        selectedTab.value = "notification"
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab.value == "list",
                        onClick = { selectedTab.value = "list" },
                        icon = { Icon(Icons.Default.List, contentDescription = "리스트") },
                        label = { Text("여행 리스트") }
                    )
                    NavigationBarItem(
                        selected = selectedTab.value == "friends",
                        onClick = { selectedTab.value = "friends" },
                        icon = { Icon(Icons.Default.Person, contentDescription = "친구") },
                        label = { Text("친구") }
                    )
                    NavigationBarItem(
                        selected = selectedTab.value == "center",
                        onClick = { selectedTab.value = "center" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "여행") },
                        label = { Text("여행") }
                    )
                    NavigationBarItem(
                        selected = selectedTab.value == "settings",
                        onClick = {
                            selectedTab.value = "settings"
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
                        label = { Text("설정") }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedTab.value) {
                    "friends" -> PlaceholderScreen("친구")

                    "center" -> {
                        when (centerTab) {
                            "addTrip" -> tripViewModel?.let {
                                AddTripScreen(viewModel = it,
                                    onTripCreated = { tripId ->
                                        navController.navigate("tripMain/$tripId") {
                                            popUpTo("addTrip") { inclusive = true }
                                        }
                                    }
                                )
                            } ?: PlaceholderScreen("뷰모델 없음")

                            "currentTrip" -> {
                                if (trip != null) {
                                    if (selectedDate != null) {
                                        CurrentTripScreen(
                                            trip = trip,
                                            schedulesForDate = schedulesForDate,
                                            selectedDate = selectedDate.value,
                                            onDateSelected = { selectedDate.value = it },
                                            onDeleteSchedule = { plan ->
                                                tripViewModel?.deleteSchedulePlan(
                                                    schedule = plan,
                                                    tripId = trip.trip_id,
                                                    date = selectedDate.value
                                                ) { success -> if (success) println("삭제됨") }
                                            },
                                            onAddScheduleClick = onAddScheduleClick,
                                            onEditScheduleClick = onEditScheduleClick,
                                            onMapClick = onMapClick,
                                            drawerState = drawerState
                                        )
                                    }
                                } else {
                                    PlaceholderScreen("여행 정보 없음")
                                }
                            }

                            else -> PlaceholderScreen("잘못된 centerTab: $centerTab")
                        }
                    }

                    "list" -> {
                        TripListScreen(
                            navController = navController,
                            viewModel = viewModel(),
                            userUid = userUid
                        )
                    }

                    "expense" -> {
                        trip?.let {
                            ExpenseMainScreen(
                                tripId = it.trip_id,
                                startDate = it.start_date.toDate().toLocalDate(),
                                endDate = it.end_date.toDate().toLocalDate(),
                                tripCountries = it.countries,
                                drawerState = drawerState,
                                tripViewModel = tripViewModel
                            )
                        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    "diary" -> PlaceholderScreen("일기")

                    "notification" -> PlaceholderScreen("알림")

                    "settings" -> settingsNavController?.let {
                        SettingsNavGraph(
                            navController = it,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${name.uppercase()} 탭 (준비 중)",
            style = MaterialTheme.typography.titleMedium
        )
    }
}