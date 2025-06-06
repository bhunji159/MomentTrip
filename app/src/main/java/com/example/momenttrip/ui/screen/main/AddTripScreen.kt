package com.example.momenttrip.ui.screen.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.momenttrip.repository.UserRepository
import com.example.momenttrip.ui.component.CountryBottomSheet
import com.example.momenttrip.ui.component.DateBottomSheet
import com.example.momenttrip.viewmodel.TripViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AddTripScreen(
    viewModel: TripViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tripName = remember { mutableStateOf("") }
    val selectedCountries = remember { mutableStateOf<List<String>>(emptyList()) }
    val selectedDates = remember { mutableStateOf<Pair<LocalDate?, LocalDate?>>(null to null) }

    val showCountrySheet = remember { mutableStateOf(false) }
    val showDateSheet = remember { mutableStateOf(false) }

    val isCreating = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    if (isCreating.value) return@Button

                    if (tripName.value.isBlank() ||
                        selectedCountries.value.isEmpty() ||
                        selectedDates.value.first == null ||
                        selectedDates.value.second == null
                    ) {
                        Toast.makeText(context, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                    } else {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            isCreating.value = true

                            viewModel.createTrip(
                                ownerUid = uid,
                                title = tripName.value,
                                startDate = selectedDates.value.first!!,
                                endDate = selectedDates.value.second!!,
                                countries = selectedCountries.value
                            ) { tripId ->
                                coroutineScope.launch {
                                    UserRepository.updateCurrentTripId(uid, tripId)
                                    delay(500)
                                    viewModel.loadCurrentTrip(tripId)

                                    // ✅ tripState가 null이 아닌 최초 값이 나올 때까지 기다림
                                    viewModel.currentTrip
                                        .filterNotNull()
                                        .first()

                                    Toast.makeText(context, "여행 생성 완료!", Toast.LENGTH_SHORT).show()
                                    isCreating.value = false

                                }
                            }

                        }
                    }
                },
                enabled = !isCreating.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("여행 만들기")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("여행 추가", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = tripName.value,
                onValueChange = { tripName.value = it },
                label = { Text("여행 이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("여행 조건", style = MaterialTheme.typography.titleMedium)

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("나라 선택", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showCountrySheet.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedCountries.value.isNotEmpty())
                                selectedCountries.value.joinToString(", ")
                            else "나라를 선택하세요"
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("여행 기간", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showDateSheet.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val (start, end) = selectedDates.value
                        Text(
                            text = if (start != null && end != null)
                                "$start ~ $end"
                            else "날짜를 선택하세요"
                        )
                    }
                }
            }
        }

        if (showCountrySheet.value) {
            CountryBottomSheet(
                onDismiss = { showCountrySheet.value = false },
                onCountriesSelected = {
                    selectedCountries.value = it
                    showCountrySheet.value = false
                }
            )
        }

        if (showDateSheet.value) {
            DateBottomSheet(
                onDismiss = { showDateSheet.value = false },
                onDateSelected = { start, end ->
                    selectedDates.value = start to end
                    showDateSheet.value = false
                }
            )
        }
    }
}
