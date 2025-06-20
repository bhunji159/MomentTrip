package com.example.momenttrip.ui.screen.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
                title = { Text("여행 리스트") }
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
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tripList.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "+ 버튼을 눌러 여행을 추가해보세요",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                tripList.forEach { trip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    navController.navigate("tripMain/${trip.trip_id}")
                                }
                        ) {
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

                        Row {
                            Button(
                                onClick = {
                                    navController.navigate("checklist/${trip.trip_id}")
                                }
                            ) {
                                Text("체크리스트")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    viewModel.deleteTrip(trip.trip_id) { success, error ->
                                        if (success) {
                                            viewModel.loadTrips(userUid) // 삭제 직후 강제 리로드
                                        } else {
                                            println("삭제 실패: $error")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
