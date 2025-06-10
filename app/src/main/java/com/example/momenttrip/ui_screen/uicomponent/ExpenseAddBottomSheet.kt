package com.example.momenttrip.ui_screen.uicomponent

import android.app.TimePickerDialog
import android.util.Log
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
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddBottomSheet(
    date: String,
    currencyOptions: List<String>,
    exchangeRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onSubmit: (ExpenseEntry) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    val fixedDate = try {
        LocalDate.parse(date)
    } catch (e: Exception) {
        onDismiss()
        return
    }

    val categoryOptions = listOf("식비", "쇼핑", "교통비", "숙박비", "관광", "카페", "항공비", "기타")
    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }

    var amount by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("카드") }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedCurrency by remember { mutableStateOf(currencyOptions.firstOrNull() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            selectedTime = LocalTime.of(hour, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    val rate = exchangeRates[selectedCurrency]
    val convertedAmount = amount.toDoubleOrNull()?.let { amt ->
        if (rate != null && rate > 0) amt * rate else null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("지출 추가", style = MaterialTheme.typography.titleMedium)

                    Text("카테고리", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
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

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedCurrency,
                            onValueChange = {},
                            label = { Text("통화 선택") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    if (convertedAmount != null) {
                        Text(
                            "환산 금액: ≈ ${"%,.0f".format(convertedAmount)} KRW",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
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
                        Text("시간 선택: ${"%02d:%02d".format(selectedTime.hour, selectedTime.minute)}")
                    }

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
                }
            }

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        Toast.makeText(context, "올바른 금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (content.isBlank()) {
                        Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val dateTime = LocalDateTime.of(fixedDate, selectedTime)
                    val timestamp = Timestamp(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))

                    val entry = ExpenseEntry(
                        amount = amountValue,
                        title = content,
                        detail = detail,
                        category = selectedCategory,
                        currency = selectedCurrency,
                        paymentType = paymentType,
                        time = timestamp
                    )
                    onSubmit(entry)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("확인")
            }
        }
    }
}