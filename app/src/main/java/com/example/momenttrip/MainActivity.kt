package com.example.momenttrip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.momenttrip.ui.theme.MomentTripTheme
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            MomentTripTheme {
                val remoteConfig = FirebaseRemoteConfig.getInstance()

                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0) // 매번 강제로 새로 가져오기
                    .build()
                remoteConfig.setConfigSettingsAsync(configSettings)

                remoteConfig.setDefaultsAsync(mapOf("EXCHANGE_RATE_API_KEY" to ""))
                remoteConfig.fetchAndActivate()
                    .addOnCompleteListener { task ->
                        val apiKey = remoteConfig.getString("EXCHANGE_RATE_API_KEY")
                        Log.d("RemoteConfig", "fetch 완료. 받은 키: '$apiKey'")
                        if (apiKey.isBlank()) {
                            Log.w("RemoteConfig", "키가 여전히 빈 문자열입니다.")
                        }
                    }

                AppEntryPoint()
//                val countryViewModel: CountryViewModel = viewModel()
//                LaunchedEffect(Unit) {
//                    countryViewModel.fetchCountries()
//                }
//                ExpenseMainScreen(
//                    tripId = "trip_id",
//                    startDate = LocalDate.parse("2025-05-01"),
//                    endDate = LocalDate.parse("2025-05-05"),
//                    tripCountries = listOf("Switzerland", "France"),
//                    allCountries = allCountries
//                )
            }
        }
    }
}

