package com.example.momenttrip.ui.screen.main

import MapViewModel
import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberFusedLocationSource

@OptIn(ExperimentalNaverMapApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    // 위치 권한 요청 및 위치 추적 설정
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    val locationSource = rememberFusedLocationSource()

    // 검색된 장소를 관리하는 상태
    var query by remember { mutableStateOf("") }

    // 장소 검색 함수
    val searchPlaces = {
        viewModel.searchPlaces(query) // 뷰모델에서 장소 검색
    }

    // 지도 및 검색 UI
    Box(modifier = Modifier.Companion.fillMaxSize()) {
        Column {
            // 검색 UI
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("장소 검색") },
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Button(
                onClick = searchPlaces,
                modifier = Modifier.Companion.padding(8.dp)
            ) {
                Text("검색")
            }
        }

        NaverMap(
            modifier = Modifier.Companion.fillMaxSize(),
            locationSource = locationSource,
            properties = MapProperties(locationTrackingMode = LocationTrackingMode.Follow)
        ) {
            // 지도에서 검색된 장소 마커 표시
            viewModel.searchResults.collectAsState().value.forEach { place ->
                val lat = place.y.toDoubleOrNull()
                val lng = place.x.toDoubleOrNull()
                if (lat != null && lng != null) {
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        captionText = place.name
                    )
                }
            }
        }
    }
}