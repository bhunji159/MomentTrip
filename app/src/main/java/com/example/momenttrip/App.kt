package com.example.momenttrip

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.momenttrip.navigation.AppNavGraph
import com.example.momenttrip.repository.UserRepository
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.screen.login.LoginViewModel
import com.example.momenttrip.ui.screen.signup.SignUpEntryPoint
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import com.example.momenttrip.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@Composable
fun AppEntryPoint() {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel()
    val tripViewModel: TripViewModel = viewModel()

    val isLoggedIn = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    val isSignUpMode = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    val tripState by tripViewModel.currentTrip.collectAsState()
    val tripCreated by tripViewModel.tripCreated.collectAsState()

    val centerTab = remember { mutableStateOf<String?>(null) }
    val navController = rememberNavController()

    var initialized by remember { mutableStateOf(false) }

    // 🔹 로그인 후 사용자 및 여행 불러오기
    LaunchedEffect(isLoggedIn.value) {
        if (isLoggedIn.value) {
            userViewModel.loadCurrentUser()
            val user = userViewModel.user.filterNotNull().first()
            val tripId = user.current_trip_id
            if (!tripId.isNullOrBlank()) {
                tripViewModel.loadCurrentTrip(tripId)
                tripViewModel.currentTrip.filterNotNull().first()
                Log.d("DEBUG111", "기존 여행 로드 완료")
            } else {
                Log.d("DEBUG111", "기존 여행 없음")
                tripViewModel.resetTrip()
                centerTab.value = "addTrip"
                initialized = true
            }
        }
    }

    // 🔹 여행 상태 기반 centerTab 결정
    LaunchedEffect(tripState) {
        Log.d("DEBUG111", "6")
        val trip = tripState
        if (trip != null) {
            Log.d("DEBUG111", "tripState가 유효함: $trip")
            val today = LocalDate.now()
            val start = trip.start_date.toDate().toLocalDate()
            val end = trip.end_date.toDate().toLocalDate()

            if (today in start..end) {
                centerTab.value = "currentTrip"
                Log.d("DEBUG111", "✅ 현재 여행 기간 안에 있음 → currentTrip")
            } else {
                val user = UserRepository.getCurrentUser()
                user?.let {
                    UserRepository.finishTrip(it.uid, trip.trip_id)
                }
                tripViewModel.resetTrip()
                centerTab.value = "addTrip"
                Log.d("DEBUG111", "⛔ 여행 기간 아님 → resetTrip + addTrip")
            }
            initialized = true
        }
        Log.d("DEBUG111", "9")
    }


    // 🔹 여행 생성 직후 처리
    LaunchedEffect(tripCreated) {
        if (tripCreated) {
            // tripState가 null이 아니게 될 때까지 기다림
            val trip = tripViewModel.currentTrip.filterNotNull().first()
            Log.d("DEBUG111", "tripState가 유효함: $trip")
            centerTab.value = "currentTrip"
            initialized = true
            tripViewModel.resetTripCreated()
        }
    }


    // 🔹 실제 렌더링 조건
    when {
        isLoggedIn.value && centerTab.value != null && initialized -> {
            AppNavGraph(
                navController = navController,
                isLoggedIn = true,
                centerTab = centerTab.value!!,
                onLogout = { isLoggedIn.value = false },
                tripViewModel = tripViewModel
            )
        }

        !isLoggedIn.value -> {
            if (isSignUpMode.value) {
                SignUpEntryPoint(
                    onSuccess = {
                        isSignUpMode.value = false
                        isLoggedIn.value = true
                    },
                    onCancel = { isSignUpMode.value = false }
                )
            } else {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    email = loginViewModel.email.value,
                    password = loginViewModel.password.value,
                    errorMessage = errorMessage.value,
                    onEmailChange = { loginViewModel.email.value = it },
                    onPasswordChange = { loginViewModel.password.value = it },
                    onLoginClick = {
                        loginViewModel.login(
                            email = loginViewModel.email.value,
                            password = loginViewModel.password.value,
                            onSuccess = { isLoggedIn.value = true },
                            onError = { errorMessage.value = it }
                        )
                    },
                    onSignUpClick = { isSignUpMode.value = true },
                    onNaverLogin = {
                        Toast.makeText(context, "네이버 로그인 준비 중", Toast.LENGTH_SHORT).show()
                    },
                    onKakaoLogin = {
                        Toast.makeText(context, "카카오 로그인 준비 중", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

