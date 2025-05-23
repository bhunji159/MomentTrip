package com.example.momenttrip.ui_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.ui_screen.uicomponent.ExpenseAddBottomSheet
import com.example.momenttrip.viewmodel.ExpenseViewModel

@Composable
fun ExpenseScreen(
    tripId: String,
    date: String,
    currency: String
) {
    val viewModel: ExpenseViewModel = viewModel()
    val expenses by viewModel.expenses.collectAsState()
    val exchangeRate by viewModel.exchangeRate.collectAsState()
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(tripId, date) {
        viewModel.loadExpenses(tripId, date)
        viewModel.loadExchangeRate(context, currency, "KRW")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$date (DAY X)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(expenses) { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val dateTime = entry.time.toDate()
                            val timeText = String.format("%02d:%02d", dateTime.hours, dateTime.minutes)
                            Text(timeText)
                            Text("${entry.title} [${entry.category}]")
                            Text("결제수단: ${entry.paymentType}")
                        }
                        Text("${entry.amount} ${entry.currency}")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Text("+")
        }

        if (showSheet) {
            ExpenseAddBottomSheet(
                date = date,
                currency = currency,
                onDismiss = { showSheet = false },
                onSubmit = { entry ->
                    viewModel.addExpense(tripId, date, entry) { success, message ->
                        if (!success) println("지출 추가 실패: $message")
                    }
                }
            )
        }
    }
}
