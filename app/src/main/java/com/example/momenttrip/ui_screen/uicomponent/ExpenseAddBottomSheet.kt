package com.example.momenttrip.ui_screen.uicomponent

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.momenttrip.data.ExpenseEntry
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddBottomSheet(
    date: String,
    currency: String,
    onDismiss: () -> Unit,
    onSubmit: (ExpenseEntry) -> Unit
) {
    val context = LocalContext.current
    val fixedDate = try {
        LocalDate.parse(date)
    } catch (e: Exception) {
        onDismiss()
        return
    }

    var amount by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("카드") }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedTime = LocalTime.of(hour, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                Text("지출 추가", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("금액") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("내용") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("시간 선택: ${"%02d:%02d".format(selectedTime.hour, selectedTime.minute)}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("결제 수단", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { paymentType = "카드" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentType == "카드") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("카드")
                    }
                    Button(onClick = { paymentType = "현금" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentType == "현금") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("현금")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            Toast.makeText(context, "올바른 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (content.isBlank()) {
                            Toast.makeText(context, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val dateTime = LocalDateTime.of(fixedDate, selectedTime)
                        val timestamp = Timestamp(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))

                        val entry = ExpenseEntry(
                            amount = amountValue,
                            title = content,
                            category = "일반",
                            currency = currency,
                            paymentType = paymentType,
                            time = timestamp
                        )
                        onSubmit(entry)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("확인")
                }
            }
        }
    }
}
