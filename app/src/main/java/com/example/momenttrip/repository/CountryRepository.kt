package com.example.momenttrip.repository

import android.util.Log
import com.example.momenttrip.api.RetrofitInstance
import com.example.momenttrip.data.CountryData
import com.example.momenttrip.data.model.CountryResponse

object CountryRepository {
    suspend fun getAllCountries(): List<CountryData> {
        return try {
            val response = RetrofitInstance.api.getAllCountries()
            response.map { country ->
                val name = country.name.common
                val currencyCode = country.currencies?.keys?.firstOrNull() ?: "N/A"
                CountryData(name, currencyCode)
            }.sortedBy { it.name }
        } catch (e: Exception) {
            Log.e("CountryRepository", "국가 데이터 로딩 실패: ${e.message}", e)
            emptyList()
        }
    }
}