package com.example.momenttrip.ui_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.data.ExpenseEntry
import com.example.momenttrip.ui.component.WeeklyCalendar
import com.example.momenttrip.ui_screen.uicomponent.AddExpense
import com.example.momenttrip.ui_screen.uicomponent.ExpenseViewDialog
import com.example.momenttrip.viewmodel.CountryViewModel
import com.example.momenttrip.viewmodel.ExpenseViewModel
import com.example.momenttrip.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseMainScreen(
    tripId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    tripCountries: List<String>,
    drawerState: DrawerState,
    tripViewModel: TripViewModel? = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf(startDate) }

    val expenseViewModel: ExpenseViewModel = viewModel()
    val countryViewModel: CountryViewModel = viewModel()
    val expenses by expenseViewModel.expenses.collectAsState()
    val exchangeRate by expenseViewModel.exchangeRate.collectAsState()
    val allCountries by countryViewModel.countries.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<ExpenseEntry?>(null) }
    var editingEntry by remember { mutableStateOf<ExpenseEntry?>(null) }

    val context = LocalContext.current

    val currencyOptions = remember(tripCountries, allCountries) {
        tripCountries.mapNotNull { country ->
            allCountries.find { it.name.trim() == country.trim() }?.currencyCode?.takeIf { it.isNotBlank() && it != "N/A" }
        }.distinct()
    }

    LaunchedEffect(currencyOptions) {
        currencyOptions.forEach { code ->
            expenseViewModel.loadExchangeRate(context, code, "KRW")
        }
    }

    LaunchedEffect(selectedDate) {
        expenseViewModel.loadExpenses(tripId, selectedDate.toString())
        countryViewModel.fetchCountries()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "가계부",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                    Icon(Icons.Default.Menu, contentDescription = "메뉴 열기")
                }
            },
            actions = {
                IconButton(onClick = { /* 공유 기능 */ }) {
                    Icon(Icons.Default.Share, contentDescription = "공유")
                }
            }
        )

        val dayIndex = ChronoUnit.DAYS.between(startDate, selectedDate).toInt() + 1

        WeeklyCalendar(
            selectedDate = selectedDate,
            onDateSelected = { newDate -> selectedDate = newDate },
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

        Text(
            "지출 내역",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )

        val totalAmountInKRW = expenses.sumOf {
            val rate = exchangeRate[it.currency]
            if (rate != null && rate > 0) it.amount * rate else 0.0
        }

        Text(
            "총합: ${"%,.0f".format(totalAmountInKRW)} KRW",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)) {
                items(expenses, key = { it.expense_id ?: it.hashCode() }) { entry ->
                    var offsetX by remember { mutableStateOf(0f) }
                    val maxSwipe = 250f

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            IconButton(onClick = {
                                entry.expense_id?.let { id ->
                                    expenseViewModel.deleteExpense(
                                        tripId,
                                        selectedDate.toString(),
                                        id
                                    ) { success, msg ->
                                        if (!success) Log.e("삭제 실패", msg ?: "")
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }

                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), 0) }
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (offsetX < -maxSwipe * 0.7f) {
                                                offsetX = 0f
                                                entry.expense_id?.let { id ->
                                                    expenseViewModel.deleteExpense(
                                                        tripId,
                                                        selectedDate.toString(),
                                                        id
                                                    ) { success, msg ->
                                                        if (!success) Log.e("삭제 실패", msg ?: "")
                                                    }
                                                }
                                            } else {
                                                offsetX = 0f
                                            }
                                        },
                                        onHorizontalDrag = { _, delta ->
                                            offsetX = (offsetX + delta).coerceIn(-maxSwipe, 0f)
                                        }
                                    )
                                }
                                .fillMaxWidth()
                        ) {
                            val rate = exchangeRate[entry.currency]
                            val converted =
                                if (rate != null && rate > 0) entry.amount * rate else null

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clickable {
                                        selectedExpense = entry
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    val firebaseTimestamp = entry.time
                                    val instant = firebaseTimestamp.toDate().toInstant()
                                    val localDateTime = java.time.LocalDateTime.ofInstant(
                                        instant,
                                        java.time.ZoneId.systemDefault()
                                    )
                                    val formattedTimeText = localDateTime.format(
                                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                                    )

                                    Text(formattedTimeText)
                                    Text("${entry.title} [${entry.category}]")
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${entry.amount} ${entry.currency} [${entry.paymentType}]")
                                    if (converted != null) {
                                        Text(
                                            "≈ ${"%,.0f".format(converted)} KRW",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else {
                                        Text(
                                            "환율 정보 없음",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(onClick = {
                editingEntry = null
                showSheet = true
            }, modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }
    }

    if (showSheet) {
        if (tripViewModel != null) {
            AddExpense(
                tripId = tripId,
                tripViewModel = tripViewModel,
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                currencyOptions = currencyOptions,
                exchangeRates = exchangeRate,
                onBack = {
                    showSheet = false
                    editingEntry = null
                },
                onSubmit = { entry, date ->
                    val originalDate = editingEntry?.time?.toDate()
                        ?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()

                    if (editingEntry == null) {
                        expenseViewModel.addExpense(tripId, date.toString(), entry) { success, msg ->
                            if (!success) Log.e("추가 실패", msg ?: "")
                            else showSheet = false
                        }
                    } else {
                        val id = editingEntry!!.expense_id ?: return@AddExpense

                        if (originalDate == date) {
                            // 날짜 안 바뀐 경우: 단순 update
                            expenseViewModel.updateExpenseFields(
                                tripId, date.toString(), id,
                                mapOf(
                                    "amount" to entry.amount,
                                    "title" to entry.title,
                                    "detail" to (entry.detail ?: ""),
                                    "category" to entry.category,
                                    "currency" to entry.currency,
                                    "paymentType" to entry.paymentType,
                                    "time" to entry.time
                                )
                            ) { success, msg ->
                                if (!success) Log.e("수정 실패", msg ?: "")
                                else {
                                    showSheet = false
                                    editingEntry = null
                                }
                            }
                        } else {
                            // 날짜 바뀐 경우: 삭제 후 새로 추가
                            expenseViewModel.deleteExpense(tripId, originalDate.toString(), id) { deleteSuccess, deleteMsg ->
                                if (!deleteSuccess) {
                                    Log.e("삭제 실패", deleteMsg ?: "")
                                    return@deleteExpense
                                }

                                expenseViewModel.addExpense(tripId, date.toString(), entry.copy(expense_id = null)) { addSuccess, addMsg ->
                                    if (!addSuccess) Log.e("추가 실패", addMsg ?: "")
                                    else {
                                        showSheet = false
                                        editingEntry = null
                                        selectedDate = date
                                        expenseViewModel.loadExpenses(tripId, date.toString())
                                    }
                                }
                            }
                        }
                    }
                },
                editingEntry = editingEntry
            )
        }
    }

    selectedExpense?.let { expense ->
        ExpenseViewDialog(
            entry = expense,
            onDismiss = { selectedExpense = null },
            onEditClick = {
                editingEntry = expense
                selectedDate =
                    expense.time.toDate().toInstant().atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                showSheet = true
                selectedExpense = null
            },
            onDeleteClick = {
                val id = expense.expense_id ?: return@ExpenseViewDialog
                expenseViewModel.deleteExpense(
                    tripId,
                    selectedDate.toString(),
                    id
                ) { success, error ->
                    if (success) selectedExpense = null
                    else Log.e("삭제 실패", error ?: "알 수 없는 오류")
                }
            }
        )
    }
}