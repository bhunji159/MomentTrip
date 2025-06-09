//package com.example.momenttrip.ui.screen.login
//
//import android.util.Log
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.momenttrip.data.LoginType
//import com.example.momenttrip.data.User
//import com.example.momenttrip.repository.UserRepository
//import com.google.firebase.Timestamp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class LoginViewModel : ViewModel() {
//
//    // 이메일과 비밀번호 상태
//    val email = mutableStateOf("")
//    val password = mutableStateOf("")
//
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
//
//    fun login(
//        email: String,
//        password: String,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        if (email.isBlank() || password.isBlank()) {
//            onError("이메일과 비밀번호를 입력해주세요.")
//            return
//        }
//
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnSuccessListener {
//                onSuccess()
//            }
//            .addOnFailureListener { e ->
//                Log.e("LoginViewModel", "로그인 실패", e)
//                onError("로그인 실패: ${e.message}")
//            }
//    }
//
//    // LoginViewModel에 추가!
//
//
//}
