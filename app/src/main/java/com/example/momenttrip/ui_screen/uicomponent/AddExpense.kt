package com.example.momenttrip.ui_screen.uicomponent

import ClickableTimeText
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momenttrip.data.ExpenseEntry
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpense(
    tripId: String,
    tripViewModel: TripViewModel,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onBack: () -> Unit,
    onSubmit: (ExpenseEntry, LocalDate) -> Unit,
    currencyOptions: List<String>,
    exchangeRates: Map<String, Double>,
    editingEntry: ExpenseEntry? = null
) {
    val context = LocalContext.current
    val tripState by tripViewModel.currentTrip.collectAsState()

    val tripStartDate = tripState?.start_date?.toDate()?.toLocalDate()
    val tripEndDate = tripState?.end_date?.toDate()?.toLocalDate()

    var selectedTime by remember {
        mutableStateOf(
            editingEntry?.time?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
                ?: LocalTime.now()
        )
    }
    var amount by remember { mutableStateOf(editingEntry?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(editingEntry?.title ?: "") }
    var detail by remember { mutableStateOf(editingEntry?.detail ?: "") }
    var selectedCategory by remember { mutableStateOf(editingEntry?.category ?: "식비") }
    var selectedCurrency by remember {
        mutableStateOf(
            editingEntry?.currency ?: currencyOptions.firstOrNull() ?: ""
        )
    }
    var paymentType by remember { mutableStateOf(editingEntry?.paymentType ?: "카드") }
    var currencyExpanded by remember { mutableStateOf(false) }

    val hour12 = if (selectedTime.hour % 12 == 0) 12 else selectedTime.hour % 12
    val amPm = if (selectedTime.hour < 12) "AM" else "PM"

    val convertedAmount = amount.toDoubleOrNull()?.let { amt ->
        exchangeRates[selectedCurrency]?.takeIf { it > 0 }?.let { amt * it }
    }

    val timePickerDialog = remember(context, selectedTime) {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute -> selectedTime = LocalTime.of(hour, minute) },
            selectedTime.hour,
            selectedTime.minute,
            false
        )
    }

    val dateFormatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
    }

    val tripDay = remember(selectedDate, tripStartDate) {
        tripStartDate?.let { (selectedDate.toEpochDay() - it.toEpochDay() + 1).toInt() }
    }

    val datePickerDialog = remember(selectedDate, tripStartDate, tripEndDate) {
        if (tripStartDate != null && tripEndDate != null) {
            val zone = ZoneId.systemDefault()
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val picked = LocalDate.of(year, month + 1, dayOfMonth)
                    if (picked in tripStartDate..tripEndDate) {
                        onDateChange(picked) // 외부 상태 업데이트
                    } else {
                        Toast.makeText(context, "여행 기간 내 날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
                    }
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).apply {
                datePicker.minDate = tripStartDate.atStartOfDay(zone).toInstant().toEpochMilli()
                datePicker.maxDate = tripEndDate.atStartOfDay(zone).toInstant().toEpochMilli()
            }
        } else null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (editingEntry != null) "지출 수정" else "지출 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                Toast.makeText(context, "금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (title.isBlank()) {
                                Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            val timestamp = Timestamp(
                                Date.from(
                                    LocalDateTime.of(selectedDate, selectedTime)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()
                                )
                            )
                            onSubmit(
                                editingEntry?.copy(
                                    amount = amt,
                                    title = title,
                                    detail = detail,
                                    category = selectedCategory,
                                    currency = selectedCurrency,
                                    paymentType = paymentType,
                                    time = timestamp
                                ) ?: ExpenseEntry(
                                    amount = amt,
                                    title = title,
                                    detail = detail,
                                    category = selectedCategory,
                                    currency = selectedCurrency,
                                    paymentType = paymentType,
                                    time = timestamp
                                ),
                                selectedDate
                            )
                            onBack()
                        },
                        enabled = title.isNotBlank() && amount.toDoubleOrNull() != null
                    ) {
                        Text("저장")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tripStartDate != null && tripEndDate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog?.show() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${selectedDate.format(dateFormatter)} · 여행 ${tripDay}일차",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 13.sp
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timePickerDialog.show() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$amPm ${"%02d:%02d".format(hour12, selectedTime.minute)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 13.sp
                        )
                    }
                }

                // 결제 수단
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("결제 수단", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("카드", "현금").forEach {
                            val isSelected = paymentType == it
                            OutlinedButton(
                                onClick = { paymentType = it },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.15f
                                    ) else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    it,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                //카테고리
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("카테고리", fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            listOf("식비", "쇼핑", "교통비", "숙박비", "관광", "카페", "항공비", "기타")
                        ) { category ->
                            val isSelected = selectedCategory == category
                            OutlinedButton(
                                onClick = { selectedCategory = category },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.15f
                                    ) else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 통화 선택
                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = !currencyExpanded }
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
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        currencyOptions.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    selectedCurrency = it
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
                }

                // 금액
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
                        fontSize = 13.sp
                    )
                }

                // 제목 & 상세
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("상세 내용") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("여행 정보 없음", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
