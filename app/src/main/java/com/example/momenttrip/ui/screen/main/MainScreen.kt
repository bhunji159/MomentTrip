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
import com.example.momenttrip.viewmodel.TripViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    centerTab: String,
    onAddScheduleClick: () -> Unit,
    onEditScheduleClick : (SchedulePlan)->Unit,
    tripViewModel: TripViewModel
) {
    val selectedTab = remember { mutableStateOf("center") }
    val tripState by tripViewModel.currentTrip.collectAsState()
    val isTripLoading by tripViewModel.isTripLoading.collectAsState()

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
                        "addTrip" -> AddTripScreen( viewModel = tripViewModel,)
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
                                        onEditScheduleClick = onEditScheduleClick
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
                "settings" -> SettingsScreen(onLogout = onLogout)
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
