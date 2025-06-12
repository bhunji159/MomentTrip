package com.example.momenttrip.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.momenttrip.ui.component.SettingsItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onAccountClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("설정", style = MaterialTheme.typography.headlineMedium)

        Divider()

        SettingsItem(title = "계정 정보", onClick = onAccountClick)

        SettingsItem(title = "알림 설정", onClick = { /* TODO */ })

        SettingsItem(title = "앱 정보", onClick = { /* TODO */ })

        SettingsItem(title = "로그아웃", onClick = {
            showLogoutDialog = true
        })
    }
}
