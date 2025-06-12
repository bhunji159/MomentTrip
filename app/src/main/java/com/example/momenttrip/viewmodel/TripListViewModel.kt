package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.Trip
import com.example.momenttrip.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripListViewModel : ViewModel() {

    private val _tripList = MutableStateFlow<List<Trip>>(emptyList())
    val tripList: StateFlow<List<Trip>> = _tripList

    fun loadTrips(ownerUid: String) {
        viewModelScope.launch {
            _tripList.value = TripRepository.getTripsByOwner(ownerUid).sortedBy { it.start_date.toDate() }
        }
    }

    fun deleteTrip(tripId: String, callback: (Boolean, String?) -> Unit) {
        _tripList.value = _tripList.value.filterNot { it.trip_id == tripId }

        viewModelScope.launch {
            val result = TripRepository.deleteTrip(tripId)
            if (!result.isSuccess) {
                loadTrips(_tripList.value.firstOrNull()?.owner_uid ?: "")
            }
            callback(result.isSuccess, result.exceptionOrNull()?.message)
        }
    }
}