package com.example.momenttrip.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.momenttrip.viewmodel.TripListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
    val dateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MomentTrip") }
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    navController.navigate("addTrip")
                },
                shape = RoundedCornerShape(50)
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            tripList.forEach { trip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("tripMain/${trip.trip_id}")
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = trip.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        val startDate = trip.start_date?.toDate()
                        val endDate = trip.end_date?.toDate()
                        val formatted = if (startDate != null && endDate != null) {
                            "${dateFormat.format(startDate)} ~ ${dateFormat.format(endDate)}"
                        } else {
                            "날짜 정보 없음"
                        }

                        Text(
                            text = formatted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = {
                            navController.navigate("checklist/${trip.trip_id}")
                        }
                    ) {
                        Text("체크리스트")
                    }
                }
            }
        }
    }
}