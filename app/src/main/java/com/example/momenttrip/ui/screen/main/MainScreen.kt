package com.example.momenttrip.ui.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.momenttrip.data.SchedulePlan
import com.example.momenttrip.ui.screen.settings.SettingsScreen
import com.example.momenttrip.ui_screen.ExpenseMainScreen
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    centerTab: String,
    onAddScheduleClick: () -> Unit,
    onEditScheduleClick: (SchedulePlan) -> Unit,
    tripViewModel: TripViewModel
) {
    val selectedTab = remember { mutableStateOf("center") }
    val tripState by tripViewModel.currentTrip.collectAsState()
    val isTripLoading by tripViewModel.isTripLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
                        onClick = { selectedTab.value = "settings" },
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
                            "addTrip" -> AddTripScreen(viewModel = tripViewModel)
                            "currentTrip" -> {
                                when {
                                    isTripLoading -> {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    tripState != null -> {
                                        CurrentTripScreen(
                                            tripViewModel = tripViewModel,
                                            onAddScheduleClick = onAddScheduleClick,
                                            onEditScheduleClick = onEditScheduleClick,
                                            drawerState = drawerState
                                        )
                                    }

                                    else -> {
                                        PlaceholderScreen("여행 정보 없음")
                                    }
                                }
                            }

                            else -> PlaceholderScreen("잘못된 centerTab: $centerTab")
                        }
                    }

                    "expense" -> {
                        if (tripState != null) {
                            val trip = tripState!!
                            ExpenseMainScreen(
                                tripId = trip.trip_id,
                                startDate = trip.start_date.toDate().toLocalDate(),
                                endDate = trip.end_date.toDate().toLocalDate(),
                                tripCountries = trip.countries,
                                drawerState = drawerState,
                                tripViewModel = tripViewModel
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    "diary" -> PlaceholderScreen("일기")

                    "notification" -> PlaceholderScreen("알림")

                    "settings" -> SettingsScreen(onLogout = onLogout)
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
