package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.momenttrip.repository.CountryRepository

class CountryViewModel : ViewModel() {
    private val _countries = MutableStateFlow<List<String>>(emptyList())
    val countries: StateFlow<List<String>> = _countries
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchCountries() {
        viewModelScope.launch {
            _isLoading.value = true
            _countries.value = CountryRepository.getAllCountries()
            _isLoading.value = false
        }
    }
}
