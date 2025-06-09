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
            _tripList.value = TripRepository.getTripsByOwner(ownerUid)
        }
    }
}
