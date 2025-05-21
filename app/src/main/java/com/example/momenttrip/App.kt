package com.example.momenttrip


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.screen.main.MainScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppEntryPoint() {
    val isLoggedIn = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    if (isLoggedIn.value) {
        MainScreen()
    } else {
        LoginScreen(
            onLoginClick = TODO(),
            onSignUpClick = TODO(),
            onNaverLogin = TODO(),
            onKakaoLogin = TODO()
        )
    }
}
