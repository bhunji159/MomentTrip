package com.example.momenttrip.ui.screen.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AddTripScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit // 로그아웃 성공 시 상위 화면에서 상태 전환
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End // 오른쪽 상단 배치
    ) {
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("로그아웃", color = MaterialTheme.colorScheme.onPrimary)
        }

        // TODO: 이후 여행 추가 UI는 여기 아래에 계속 추가하면 됨
    }
}
