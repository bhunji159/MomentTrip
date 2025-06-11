package com.example.momenttrip.repository

import android.content.Context
import com.example.momenttrip.api.ExchangeRateService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object ExchangeRateRepository {

    private suspend fun getApiKey(): String {
        // Remote Config에서 키 받아오기 (비동기)
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        // fetchAndActivate()가 이미 실행됐다는 가정하에 바로 가져옴
        return remoteConfig.getString("EXCHANGE_RATE_API_KEY")
    }

    suspend fun getRateWithCache(context: Context, base: String, target: String): Result<Double> {
        return withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("exchange_rate_cache", Context.MODE_PRIVATE)
            val today = LocalDate.now().toEpochDay()
            val lastUpdated = prefs.getLong("last_updated_day", -1L)
            val cacheKey = "rate_${base}_$target"

            // 캐시된 환율이 오늘 날짜면 그대로 사용
            if (lastUpdated == today) {
                val cached = prefs.getFloat(cacheKey, -1f)
                if (cached >= 0) return@withContext Result.success(cached.toDouble())
            }

            try {
                val apiKey = getApiKey()
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(Exception("API 키가 설정되지 않았습니다."))
                }

                val response = ExchangeRateService.api.getLatestRates(apiKey, base)
                val rate = response.conversion_rates[target]
                    ?: return@withContext Result.failure(Exception("환율 없음: $target"))

                // 캐시에 저장
                prefs.edit()
                    .putFloat(cacheKey, rate.toFloat())
                    .putLong("last_updated_day", today)
                    .apply()

                Result.success(rate)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
