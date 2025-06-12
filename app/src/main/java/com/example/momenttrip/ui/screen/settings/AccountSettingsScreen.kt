package com.example.momenttrip.ui.screen.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.momenttrip.R
import com.example.momenttrip.viewmodel.SettingsViewModel

@Composable
fun AccountSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
) {
    val userState by viewModel.user.collectAsState()
    val nicknameResult by viewModel.nicknameUpdateResult.collectAsState()
    val profileImageResult by viewModel.profileImageUpdateResult.collectAsState()

    var newNickname by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            viewModel.updateProfileImage(it)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        userState?.let { user ->
            // 프로필 이미지
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = user.profile_url ?: R.drawable.baseline_account_circle_24,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .clickable {
                            // 이미지 선택 (갤러리 열기)
                            launcher.launch("image/*")
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 닉네임 수정
            OutlinedTextField(
                value = newNickname,
                onValueChange = { newNickname = it },
                label = { Text("닉네임 수정") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (newNickname.isNotBlank()) {
                        viewModel.updateNickname(newNickname)
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("닉네임 변경")
            }

            // 변경 결과 메시지
            nicknameResult.takeIf { it.second != null }?.second?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            profileImageResult.takeIf { it.second != null }?.second?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
