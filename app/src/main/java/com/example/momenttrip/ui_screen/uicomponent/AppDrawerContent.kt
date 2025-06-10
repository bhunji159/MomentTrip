package com.example.momenttrip.ui_screen.uicomponent

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppDrawer(
    selectedScreen: String,
    onScreenSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        NavigationDrawerItem(
            label = { Text("여행 일정") },
            selected = selectedScreen == "schedule",
            onClick = { onScreenSelected("schedule") }
        )
        NavigationDrawerItem(
            label = { Text("가계부") },
            selected = selectedScreen == "expense",
            onClick = { onScreenSelected("expense") }
        )
        NavigationDrawerItem(
            label = { Text("일기") },
            selected = selectedScreen == "diary",
            onClick = { onScreenSelected("diary") }
        )
        // 추가 메뉴 가능
    }
}