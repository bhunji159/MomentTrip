package com.example.momenttrip.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.ExpenseEntry
import com.example.momenttrip.repository.ExchangeRateRepository
import com.example.momenttrip.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {
    private val _expenses = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntry>> = _expenses

    private val _exchangeRate = MutableStateFlow<Double?>(null)
    val exchangeRate: StateFlow<Double?> = _exchangeRate


    //특정 날짜에 기록된 지출 목록
    fun loadExpenses(tripId: String, date: String) {
        viewModelScope.launch {
            val result = ExpenseRepository.getExpenses(tripId, date)
            if (result.isSuccess) {
                _expenses.value = result.getOrNull()?.sortedBy { it.time.toDate() } ?: emptyList()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "알 수 없는 오류"
                Log.e("ExpenseViewModel", "지출 항목 불러오기 실패: $errorMsg")
            }
        }
    }

    //지출 항목 추가
    fun addExpense(tripId: String, date: String, entry: ExpenseEntry, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = ExpenseRepository.addExpense(tripId, date, entry)
            if (result.isSuccess) {
                loadExpenses(tripId, date)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    //지출 항목 삭제
    fun deleteExpense(tripId: String, date: String, expenseId: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = ExpenseRepository.deleteExpense(tripId, date, expenseId)
            if (result.isSuccess) {
                loadExpenses(tripId, date)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    //지출 항목 수정
    fun updateExpenseFields(
        tripId: String,
        date: String,
        expenseId: String,
        updatedFields: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = ExpenseRepository.updateExpenseFields(tripId, date, expenseId, updatedFields)
            if (result.isSuccess) {
                loadExpenses(tripId, date)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    //환율 불러오기
    fun loadExchangeRate(context: Context, base: String, target: String) {
        viewModelScope.launch {
            val result = ExchangeRateRepository.getRateWithCache(context, base, target)
            if (result.isSuccess) {
                _exchangeRate.value = result.getOrNull()
            } else {
                // 에러 로그 출력
                Log.e("ExpenseViewModel", "환율 로드 실패: ${result.exceptionOrNull()?.message}")
            }
        }
    }


}