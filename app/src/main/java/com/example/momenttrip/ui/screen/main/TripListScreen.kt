package com.example.momenttrip.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.momenttrip.ui.components.TripItem
import com.example.momenttrip.viewmodel.TripListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    navController: NavController,
    viewModel: TripListViewModel = viewModel(),
    userUid: String
) {
    LaunchedEffect(Unit) {
        viewModel.loadTrips(userUid)
    }

    val tripList by viewModel.tripList.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("이전 여행 목록") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            tripList.forEach { trip ->
                TripItem(
                    trip = trip,
                    onDiaryClick = { navController.navigate("diary/${trip.trip_id}") },
                    onChecklistClick = { navController.navigate("checklist/${trip.trip_id}") }
                )
            }
        }
    }
}
