package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.Trip
import com.example.momenttrip.data.model.CheckListItem
import com.example.momenttrip.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CheckListViewModel: ViewModel() {
    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _trip.value = TripRepository.getTripById(tripId)
        }
    }

    private val _items = MutableStateFlow<List<CheckListItem>>(emptyList())
    val items: StateFlow<List<CheckListItem>> = _items

    fun loadChecklist(tripId: String) {
        viewModelScope.launch {
            _items.value = TripRepository.getChecklistItems(tripId)
        }
    }

    fun toggleItem(tripId: String, item: CheckListItem) {
        val updated = item.copy(isChecked = !item.isChecked)
        viewModelScope.launch {
            TripRepository.updateChecklistItem(tripId, updated)
            loadChecklist(tripId)
        }
    }

    fun addItem(tripId: String, content: String) {
        viewModelScope.launch {
            TripRepository.addChecklistItem(tripId, content)
            loadChecklist(tripId)
        }
    }

    fun deleteItem(tripId: String, itemId: String) {
        viewModelScope.launch {
            TripRepository.deleteChecklistItem(tripId, itemId)
            loadChecklist(tripId)
        }
    }


}