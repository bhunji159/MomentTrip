package com.example.momenttrip.ui.component

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateBottomSheet(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate, LocalDate) -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("여행 기간 선택", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            DatePickerField(
                label = "시작 날짜",
                selectedDate = startDate,
                onDateSelected = { startDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DatePickerField(
                label = "종료 날짜",
                selectedDate = endDate,
                onDateSelected = { endDate = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (startDate != null && endDate != null) {
                        if (endDate!!.isBefore(startDate)) {
                            Toast.makeText(context, "종료일은 시작일 이후여야 합니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            onDateSelected(startDate!!, endDate!!)
                            onDismiss()
                        }
                    }
                },
                enabled = startDate != null && endDate != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("선택 완료")
            }
        }
    }
}
