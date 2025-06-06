package com.example.momenttrip.ui.screen.addSchedule

import ClickableTimeText
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.momenttrip.ui.component.ClickableDateText
import com.example.momenttrip.ui.component.TopAppBarWithIcon
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AddScheduleScreen(
    onBack: () -> Unit,
    tripViewModel: TripViewModel
) {
    val tripState by tripViewModel.currentTrip.collectAsState()
    val context = LocalContext.current
    val today = remember { LocalDate.now() }

    var selectedDate by remember { mutableStateOf(today) }

    val tripStartDate = tripState?.start_date?.toDate()?.toLocalDate()
    val tripEndDate = tripState?.end_date?.toDate()?.toLocalDate()

    var selectedStartTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedEndTime by remember { mutableStateOf(LocalTime.now().plusMinutes(30)) }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBarWithIcon(
                title = "일정 추가",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onBack,
                actionIcon = {
                    TextButton(
                        onClick = {
                            if (uid != null && tripState != null) {
                                val trip = tripState!!
                                tripViewModel.addSchedulePlan(
                                    tripId = trip.trip_id,
                                    date = selectedDate,
                                    title = title,
                                    content = content,
                                    startTime = selectedStartTime,
                                    endTime = selectedEndTime,
                                    authorUid = uid,
                                    onComplete = { success ->
                                        Toast.makeText(
                                            context,
                                            if (success) "일정 저장 완료" else "일정 저장 실패",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        if (success) onBack()
                                    }
                                )
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("저장")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (tripStartDate != null && tripEndDate != null) {
                ClickableDateText(
                    selectedDate = selectedDate,
                    tripStartDate = tripStartDate,
                    tripEndDate = tripEndDate,
                    onDateSelected = { selectedDate = it },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("시작 시간", fontWeight = FontWeight.Bold)
                        ClickableTimeText(
                            selectedTime = selectedStartTime,
                            onTimeSelected = {
                                selectedStartTime = it
                                if (selectedEndTime <= it) {
                                    selectedEndTime = it.plusMinutes(30)
                                }
                            },
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("종료 시간", fontWeight = FontWeight.Bold)
                        ClickableTimeText(
                            selectedTime = selectedEndTime,
                            onTimeSelected = { selectedEndTime = it },
                        )
                    }
                }
            } else {
                Text("여행 정보 없음", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = false,
                maxLines = 6,
                minLines = 3
            )
        }
    }
}