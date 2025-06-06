package com.example.momenttrip.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.momenttrip.utils.getWeekDates
import java.time.LocalDate

@Composable
fun WeeklyCalendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
) {
    val weekDates = getWeekDates(selectedDate)

    Column(modifier = modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                // 이전 주 첫날 날짜로 변경 요청
                onDateSelected(selectedDate.minusWeeks(1))
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "이전 주")
            }
            Text(
                "${selectedDate.year}년 ${selectedDate.monthValue}월",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = {
                // 다음 주 첫날 날짜로 변경 요청
                onDateSelected(selectedDate.plusWeeks(1))
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "다음 주")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDates.forEach { date ->
                val isSelected = date == selectedDate
                val inRange =
                    startDate != null && endDate != null && !date.isBefore(startDate) && !date.isAfter(endDate)

                val bgColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    inRange -> MaterialTheme.colorScheme.tertiaryContainer // 연한 색
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor =
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(4.dp)
                        .then(
                            if (inRange) Modifier.clickable { onDateSelected(date) }
                            else Modifier // 클릭 불가 상태면 클릭리스너 없음
                        )
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            java.util.Locale.KOREAN
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(40.dp)
                            .background(color = bgColor, shape = CircleShape)
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


