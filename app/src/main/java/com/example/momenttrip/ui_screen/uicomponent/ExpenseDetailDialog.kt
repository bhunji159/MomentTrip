package com.example.momenttrip.ui_screen.uicomponent

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailDialog(
    tripId: String,
    date: String,
    entry: ExpenseEntry,
    currencyOptions: List<String>,
    exchangeRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onUpdate: (Map<String, Any>) -> Unit
) {
    val context = LocalContext.current

    val categoryOptions = listOf("식비", "쇼핑", "교통비", "숙박비", "관광", "카페", "항공비", "기타")
    var selectedCategory by remember { mutableStateOf(entry.category.ifBlank { categoryOptions.first() }) }

    var amount by remember { mutableStateOf(entry.amount.toString()) }
    var title by remember { mutableStateOf(entry.title) }
    var detail by remember { mutableStateOf(entry.detail ?: "") }
    var paymentType by remember { mutableStateOf(entry.paymentType) }
    var selectedCurrency by remember {
        mutableStateOf(entry.currency ?: currencyOptions.firstOrNull() ?: "USD")
    }
    var time by remember {
        mutableStateOf(entry.time?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime() ?: LocalTime.now())
    }

    var expanded by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute -> time = LocalTime.of(hour, minute) },
        time.hour, time.minute, true
    )

    val rate = exchangeRates[selectedCurrency]
    val convertedAmount = amount.toDoubleOrNull()?.let { amt ->
        if (rate != null && rate > 0) amt * rate else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = { Text("지출 수정") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // LazyColumn for scrollable content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // 카테고리
                        Text("카테고리", style = MaterialTheme.typography.labelLarge)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            items(categoryOptions) { category ->
                                Button(
                                    onClick = { selectedCategory = category },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedCategory == category)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text(category)
                                }
                            }
                        }

                        // 통화 선택
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = selectedCurrency,
                                onValueChange = {},
                                label = { Text("통화 선택") },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                currencyOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedCurrency = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("금액") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )

                        if (convertedAmount != null) {
                            Text(
                                "환산 금액: ≈ ${"%,.0f".format(convertedAmount)} KRW",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("제목") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = detail,
                            onValueChange = { detail = it },
                            label = { Text("상세 내용") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = { timePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("시간 선택: ${"%02d:%02d".format(time.hour, time.minute)}")
                        }

                        Text("결제 수단", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { paymentType = "카드" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentType == "카드") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("카드")
                            }
                            Button(
                                onClick = { paymentType = "현금" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (paymentType == "현금") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("현금")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 수정/취소 버튼 고정
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    TextButton(onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            Toast.makeText(context, "올바른 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        if (title.isBlank()) {
                            Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        val fixedDate = LocalDate.parse(date)
                        val dateTime = LocalDateTime.of(fixedDate, time)
                        val timestamp = Timestamp(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))

                        val updatedData = mapOf(
                            "amount" to amountValue,
                            "title" to title,
                            "detail" to detail,
                            "paymentType" to paymentType,
                            "currency" to selectedCurrency,
                            "category" to selectedCategory,
                            "time" to timestamp
                        )
                        onUpdate(updatedData)
                        onDismiss()
                    }) {
                        Text("수정")
                    }
                }
            }
        }
    )
}
