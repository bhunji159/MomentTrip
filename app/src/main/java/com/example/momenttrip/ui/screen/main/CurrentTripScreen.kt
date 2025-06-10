package com.example.momenttrip.ui.screen.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.data.SchedulePlan
import com.example.momenttrip.ui.component.ScheduleDetailDialog
import com.example.momenttrip.ui.component.ScheduleList
import com.example.momenttrip.ui.component.TopAppBarWithIcon
import com.example.momenttrip.ui.component.WeeklyCalendar
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit


@Composable
fun CurrentTripScreen(
    tripViewModel: TripViewModel = viewModel(),
    onAddScheduleClick: () -> Unit,
    onEditScheduleClick: (SchedulePlan) -> Unit,
    drawerState: DrawerState
) {
    val tripState by tripViewModel.currentTrip.collectAsState()
    val isLoading by tripViewModel.isTripLoading.collectAsState()
    val schedulesForDate by tripViewModel.schedulesForDate.collectAsState()
    val selectedDate by tripViewModel.selectedDate.collectAsState()
    val now = LocalTime.now()

    val coroutineScope = rememberCoroutineScope()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<SchedulePlan?>(null) }

    var deleteTarget by remember { mutableStateOf<SchedulePlan?>(null) }
    val firstUpcomingTime = schedulesForDate
        .filter {
            val start = LocalTime.parse(it.start_time)
            start > now
        }
        .minByOrNull {
            LocalTime.parse(it.start_time)
        }

    LaunchedEffect(selectedDate, tripState) {
        tripState?.let { trip ->
            tripViewModel.loadSchedulePlans(trip.trip_id, selectedDate)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        TopAppBarWithIcon(
            title = "여행일정",
            navigationIcon = Icons.Default.Menu,
            onNavigationClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            },
            actionIcon = {
                IconButton(onClick = { /* 공유 */ }) {
                    Icon(Icons.Default.Share, contentDescription = "공유")
                }
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                tripState == null -> {
                    Text(
                        text = "현재 여행 정보가 없습니다.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    val trip = tripState!!
                    val startDate = trip.start_date.toDate().toLocalDate()
                    val endDate = trip.end_date.toDate().toLocalDate()
                    val dayIndex = ChronoUnit.DAYS.between(startDate, selectedDate).toInt() + 1
                    Log.d("CurrentTripScreen", "WeeklyCalendar 호출 selectedDate: $selectedDate")

                    WeeklyCalendar(
                        selectedDate = selectedDate,
                        onDateSelected = { newDate ->
                            tripViewModel.updateSelectedDate(newDate)
                        },
                        startDate = startDate,
                        endDate = endDate
                    )

                    Text(
                        text = "${selectedDate.year}.${selectedDate.monthValue}.${selectedDate.dayOfMonth} (DAY $dayIndex)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // LazyColumn 영역 weight(1f)로 감싸서 버튼과 겹치지 않게
                    val sortedSchedules =
                        schedulesForDate.sortedBy { LocalTime.parse(it.start_time) }
                    ScheduleList(
                        schedulesForDate = sortedSchedules,
                        firstUpcomingTime = firstUpcomingTime,
                        onDetailClick = { schedule ->
                            selectedSchedule = schedule
                            showDetailDialog = true
                        },
                        onDelete = { plan ->
                            deleteTarget = plan
                        }
                    )
                    if (deleteTarget != null) {
                        AlertDialog(
                            onDismissRequest = { deleteTarget = null },
                            title = { Text("일정 삭제") },
                            text = { Text("정말로 이 일정을 삭제하시겠습니까?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        tripViewModel.deleteSchedulePlan(
                                            schedule = deleteTarget!!,
                                            tripId = tripState!!.trip_id,
                                            date = selectedDate
                                        ) { /* 필요시 토스트 등 */ }
                                        deleteTarget = null
                                    }
                                ) { Text("삭제") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { deleteTarget = null }
                                ) { Text("취소") }
                            }
                        )
                    }
                    if (showDetailDialog && selectedSchedule != null) {
                        ScheduleDetailDialog(
                            schedule = selectedSchedule!!,
                            onDismiss = { showDetailDialog = false },
                            onEditClick = {
                                onEditScheduleClick(selectedSchedule!!)
                                showDetailDialog = false
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = { /* 지도 이동 */ },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("지도")
            }

            Button(
                onClick = { onAddScheduleClick() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+")
            }
        }

    }
}

