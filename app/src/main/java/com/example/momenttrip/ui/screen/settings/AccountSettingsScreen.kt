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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val userState by viewModel.user.collectAsState()
    val nicknameResult by viewModel.nicknameUpdateResult.collectAsState()
    val profileImageResult by viewModel.profileImageUpdateResult.collectAsState()

    var newNickname by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // 닉네임 변경 결과 메시지를 Snackbar로 표시 (성공 + 실패 둘 다)
    LaunchedEffect(nicknameResult) {
        val message = nicknameResult.second
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
        }
    }

    // 최초 사용자 정보 불러오기
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("계정 정보") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
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
                                launcher.launch("image/*")
                            }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 현재 닉네임
                Text(
                    text = "현재 닉네임: ${user.nickname}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )

                // 닉네임 수정 입력
                OutlinedTextField(
                    value = newNickname,
                    onValueChange = { newNickname = it },
                    label = { Text("닉네임 수정") },
                    singleLine = true,
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth()
                )

                // 닉네임 변경 버튼
                Button(
                    onClick = {
                        if (newNickname.isNotBlank()) {
                            viewModel.updateNickname(newNickname)
                            newNickname = ""
                        }
                    },
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text("닉네임 변경")
                }

                // 프로필 이미지 변경 실패 메시지 (텍스트로 표시)
                profileImageResult.takeIf { it.second != null }?.second?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}