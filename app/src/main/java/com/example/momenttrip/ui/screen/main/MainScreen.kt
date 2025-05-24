package com.example.momenttrip.ui.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun MainScreen(
    onLogout: () -> Unit // 로그아웃 시 상위로 전달
) {
    val isTraveling = remember { mutableStateOf(false) } // 나중에 Firestore에서 현재 여행 여부 확인 예정

    if (isTraveling.value) {
        CurrentTripScreen()
    } else {
        AddTripScreen(
            onLogout = onLogout
        )
    }
}
