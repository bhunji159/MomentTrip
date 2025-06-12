package com.example.momenttrip.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.momenttrip.data.SchedulePlan
import com.example.momenttrip.data.Trip
import com.example.momenttrip.ui.component.ScheduleDetailDialog
import com.example.momenttrip.ui.component.ScheduleList
import com.example.momenttrip.ui.component.TopAppBarWithIcon
import com.example.momenttrip.ui.component.WeeklyCalendar
import com.example.momenttrip.utils.toLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
fun CurrentTripScreen(
    trip: Trip,
    schedulesForDate: List<SchedulePlan>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteSchedule: (SchedulePlan) -> Unit,
    onAddScheduleClick: () -> Unit,
    onEditScheduleClick: (SchedulePlan) -> Unit,
    onMapClick: () -> Unit,
    drawerState: DrawerState
) {
    val now = LocalTime.now()
    val coroutineScope = rememberCoroutineScope()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<SchedulePlan?>(null) }
    var deleteTarget by remember { mutableStateOf<SchedulePlan?>(null) }

    val firstUpcomingTime = schedulesForDate
        .filter { LocalTime.parse(it.start_time) > now }
        .minByOrNull { LocalTime.parse(it.start_time) }

    val startDate = trip.start_date.toDate().toLocalDate()
    val endDate = trip.end_date.toDate().toLocalDate()
    val dayIndex = ChronoUnit.DAYS.between(startDate, selectedDate).toInt() + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        TopAppBarWithIcon(
            title = "여행일정",
            navigationIcon = Icons.Default.Menu,
            onNavigationClick = {
                coroutineScope.launch { drawerState.open() }
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
            WeeklyCalendar(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
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

            val sortedSchedules = schedulesForDate.sortedBy { LocalTime.parse(it.start_time) }
            ScheduleList(
                schedulesForDate = sortedSchedules,
                firstUpcomingTime = firstUpcomingTime,
                onDetailClick = {
                    selectedSchedule = it
                    showDetailDialog = true
                },
                onDelete = {
                    deleteTarget = it
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
                                onDeleteSchedule(deleteTarget!!)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = onMapClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("지도")
            }

            Button(
                onClick = onAddScheduleClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+")
            }
        }
    }
}