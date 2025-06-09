package com.example.momenttrip.ui.screen.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GoogleSignUpExtraScreen(
    defaultName: String,
    onSubmit: (String, String, String) -> Unit,
    onBack: () -> Unit  // ★ 추가!
) {
    BackHandler(onBack = onBack)
    var name by remember { mutableStateOf(defaultName) }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val inputModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("추가 정보 입력", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            singleLine = true,
            modifier = inputModifier
        )
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("닉네임") },
            singleLine = true,
            modifier = inputModifier
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { input ->
                phone = input.filter { it.isDigit() }
            },
            label = { Text("전화번호") },
            singleLine = true,
            modifier = inputModifier,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onSubmit(name, nickname, phone) },
            enabled = name.isNotBlank() && nickname.isNotBlank() && phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("회원가입 완료", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        // ★ 뒤로가기 버튼 추가
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text("뒤로가기")
        }
    }
}
