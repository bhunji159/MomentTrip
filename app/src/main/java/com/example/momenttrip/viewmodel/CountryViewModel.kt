package com.example.momenttrip.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.CountryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.momenttrip.repository.CountryRepository

class CountryViewModel : ViewModel() {
    private val _countries = MutableStateFlow<List<CountryData>>(emptyList())
    val countries: StateFlow<List<CountryData>> = _countries
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchCountries() {
        Log.d("CountryViewModel", "fetchCountries() 호출됨")

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = CountryRepository.getAllCountries()
                Log.d("CountryViewModel", "받아온 나라 수: ${result.size}")
                _countries.value = result
            } catch (e: Exception) {
                Log.e("CountryViewModel", "국가 가져오기 실패", e)
            }

            _isLoading.value = false
        }
    }
}