package com.example.momenttrip.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ClickableDateText(
    selectedDate: LocalDate,
    tripStartDate: LocalDate,
    tripEndDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        DatePickerModal(
            minDate = tripStartDate,
            maxDate = tripEndDate
        ) {
            onDateSelected(it)
            showDialog = false
        }
    }

    val daysElapsed = ChronoUnit.DAYS.between(tripStartDate, selectedDate) + 1
    val formatted = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
    val dateInfo = "$formatted · 여행 ${daysElapsed}일차"

    Text(
        text = dateInfo,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 8.dp)
    )
}

