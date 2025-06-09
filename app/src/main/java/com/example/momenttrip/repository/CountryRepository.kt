package com.example.momenttrip.repository

import com.example.momenttrip.api.RetrofitInstance
import com.example.momenttrip.data.model.CountryResponse

object CountryRepository {
    suspend fun getAllCountries(): List<String> {
        return try {
            val response = RetrofitInstance.api.getAllCountries()
            response.map { it.name.common }.sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
