package com.example.momenttrip.ui.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun MainScreen() {
    val isTraveling = remember { mutableStateOf(false) } // 임시. 나중엔 DB 조회 등으로 바뀜

    if (isTraveling.value) {
        CurrentTripScreen()
    } else {
        AddTripScreen()
    }
}
