package com.example.momenttrip.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.SchedulePlan
import com.example.momenttrip.data.Trip
import com.example.momenttrip.repository.TripRepository
import com.example.momenttrip.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TripViewModel : ViewModel() {

    private val _tripCreated = MutableStateFlow(false)
    val tripCreated: StateFlow<Boolean> = _tripCreated

    private val _currentTrip = MutableStateFlow<Trip?>(null)
    val currentTrip: StateFlow<Trip?> = _currentTrip

    private val _isTripLoading = MutableStateFlow(false)
    val isTripLoading: StateFlow<Boolean> = _isTripLoading

    private val _schedulesForDate = MutableStateFlow<List<SchedulePlan>>(emptyList())
    val schedulesForDate: StateFlow<List<SchedulePlan>> = _schedulesForDate

    private val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    suspend fun loadCurrentTrip(tripId: String) {
        _isTripLoading.value = true
        val trip = TripRepository.getTripById(tripId)
        _currentTrip.value = trip
        _isTripLoading.value = false
    }

    fun resetTrip() {
        _currentTrip.value = null
    }
    fun createTrip(
        ownerUid: String,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate,
        countries: List<String>,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripRepository.createTrip(ownerUid, title, startDate, endDate, countries)
            Log.d("DEBUG111", "1")
            result.onSuccess { tripId ->
                _isTripLoading.value = true
                UserRepository.updateCurrentTripId(ownerUid, tripId)
                Log.d("DEBUG111", "2")
                // 🔥 trip 먼저 가져오고, null 아니면 그때 tripCreated true로!
                val trip = TripRepository.getTripById(tripId)
                _currentTrip.value = trip
                Log.d("DEBUG111", "3")
                if (trip != null) {
                    _tripCreated.value = true // 여기서 trip이 null 아니면 확실하게 트리거
                    onCreated(tripId)
                    Log.d("DEBUG111", "4")
                }
                Log.d("DEBUG111", "5")
                _isTripLoading.value = false
            }
        }
    }


    fun resetTripCreated() {
        _tripCreated.value = false
    }

    fun deleteTrip(tripId: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = TripRepository.deleteTrip(tripId)
            callback(result.isSuccess, result.exceptionOrNull()?.message)
        }
    }

    fun addSchedulePlan(
        tripId: String,
        date: LocalDate,
        title: String,
        content: String,
        startTime: java.time.LocalTime,
        endTime: java.time.LocalTime,
        authorUid: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripRepository.addSchedulePlan(
                tripId = tripId,
                date = date,
                title = title,
                content = content,
                startTime = startTime,
                endTime = endTime,
                authorUid = authorUid
            )
            if (result.isSuccess) {
                // 일정 추가 성공 시 선택 날짜의 스케줄 다시 불러오기
                _currentLoadedDate.value = null
                loadSchedulePlans(tripId, date)
            }
            onComplete(result.isSuccess)
        }
    }

    private val _currentLoadedDate = MutableStateFlow<LocalDate?>(null)
    fun loadSchedulePlans(tripId: String, date: LocalDate) {
        if (_currentLoadedDate.value == date) return // 중복 호출 방지

        viewModelScope.launch {
            _isTripLoading.value = true
            val plans = TripRepository.getSchedulePlans(tripId, date) // 새로 만들어야 함
            _schedulesForDate.value = plans
            _currentLoadedDate.value = date
            _isTripLoading.value = false
        }
    }
    fun updateSchedulePlan(
        editingSchedule: SchedulePlan,
        tripId: String,
        date: LocalDate,
        title: String,
        content: String,
        startTime: LocalTime,
        endTime: LocalTime,
        authorUid: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripRepository.updateSchedulePlan(
                tripId = tripId,
                date = date,
                planId = editingSchedule.documentId, // 문서 ID로 접근!
                title = title,
                content = content,
                startTime = startTime,
                endTime = endTime,
                authorUid = authorUid
            )
            if (result.isSuccess) {
                _currentLoadedDate.value = null
                loadSchedulePlans(tripId, date)
            }
            onComplete(result.isSuccess)
        }
    }

    fun deleteSchedulePlan(
        schedule: SchedulePlan,
        tripId: String,
        date: LocalDate,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = TripRepository.deleteSchedulePlan(
                tripId = tripId,
                date = date,
                planId = schedule.documentId
            )
            if (result.isSuccess) {
                _currentLoadedDate.value = null
                loadSchedulePlans(tripId, date)
            }
            onComplete(result.isSuccess)
        }
    }

}