package com.example.momenttrip.ui.screen.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Date

class SignUpViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signUp(
        email: String,
        password: String,
        name: String,
        nickname: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    val user = hashMapOf(
                        "email" to email,
                        "login_type" to "email",
                        "name" to name,
                        "nickname" to nickname,
                        "phone_number" to phoneNumber,
                        "profile_url" to "",
                        "created_at" to Date(),
                        "current_trip_id" to null,
                        "search_key" to listOf(email.lowercase(), nickname.trim(), phoneNumber.trim()),
                        "friends" to emptyList<String>(),
                        "friend_request_ids" to emptyList<String>(),
                        "trip_invite_ids" to emptyList<String>()
                    )

                    firestore.collection("users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            // 🔥 자동 로그인 처리
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    onError("자동 로그인 실패: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            onError("Firestore 저장 실패: ${e.message}")
                        }

                }
                .addOnFailureListener { e ->
                    Log.e("SignUpViewModel", "회원가입 실패", e)
                    onError("회원가입 실패: ${e.message}")
                }
        }
    }
}
