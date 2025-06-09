package com.example.momenttrip.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.momenttrip.R
import com.example.momenttrip.data.User
import com.example.momenttrip.ui.component.SettingsItem
import com.example.momenttrip.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToProfile: () -> Unit = {},   // 계정(프로필) 화면 이동 콜백
    onLogout: () -> Unit = {},              // 로그아웃 후 이동 콜백
    onNavigateToTerms: () -> Unit = {},     // 약관/정책 이동 콜백
) {
    val user by viewModel.user.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    val context = LocalContext.current

    // 로그아웃 완료 시 콜백 실행
    if (logoutState) {
        onLogout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 프로필 이미지 및 닉네임
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (user?.profile_url != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(user?.profile_url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.size(100.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                    contentDescription = "기본 프로필",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = user?.nickname ?: "",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 설정 항목 리스트
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 계정(프로필) 설정 이동
            SettingsItem(title = "계정", onClick = onNavigateToProfile)
            Spacer(modifier = Modifier.height(8.dp))

            // 로그아웃
            SettingsItem(
                title = "로그아웃",
                onClick = { viewModel.logout() }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 이용약관 및 개인정보처리방침
            SettingsItem(
                title = "이용약관 및 개인정보처리방침",
                onClick = onNavigateToTerms
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    // 더미 ViewModel 생성 (실제 앱에서는 ViewModelProvider 사용)
    val dummyViewModel = object : SettingsViewModel() {
        init {
            _user.value = User(
                uid = "test_uid",
                email = "test@example.com",
                name = "홍길동",
                nickname = "길동이",
                phone_number = "010-1234-5678",
                profile_url = null // 또는 임의의 이미지 URL
            )
        }
    }

    SettingsScreen(
        viewModel = dummyViewModel,
        onNavigateToProfile = {},
        onLogout = {},
        onNavigateToTerms = {}
    )
}
