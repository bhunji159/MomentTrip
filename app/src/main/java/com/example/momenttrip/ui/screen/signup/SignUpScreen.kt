package com.example.momenttrip.ui.screen.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    email: String,
    password: String,
    confirmPassword: String,
    name: String,
    nickname: String,
    phoneNumber: String,
    errorMessage: String,
    emailCheckMessage: String,
    nicknameCheckMessage: String,
    phoneCheckMessage: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("이메일") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (emailCheckMessage.isNotEmpty()) {
            Text(
                text = emailCheckMessage,
                color = if (emailCheckMessage.contains("가능")) Color.Green else Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("비밀번호 확인") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            isError = confirmPassword.isNotEmpty() && confirmPassword != password,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("이름") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("닉네임") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (nicknameCheckMessage.isNotEmpty()) {
            Text(
                text = nicknameCheckMessage,
                color = if (nicknameCheckMessage.contains("가능")) Color.Green else Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("전화번호") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (phoneCheckMessage.isNotEmpty()) {
            Text(
                text = phoneCheckMessage,
                color = if (phoneCheckMessage.contains("등록")) Color.Red else Color.Green,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("회원가입")
        }
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("뒤로가기")
        }
    }
}
