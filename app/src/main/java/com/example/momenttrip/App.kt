package com.example.momenttrip

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.momenttrip.data.LoginType
import com.example.momenttrip.data.User
import com.example.momenttrip.navigation.AppNavGraph
import com.example.momenttrip.repository.UserRepository
import com.example.momenttrip.ui.screen.loading.AppLoadingScreen
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.screen.signup.GoogleSignUpExtraScreen
import com.example.momenttrip.ui.screen.signup.SignUpEntryPoint
import com.example.momenttrip.utils.toLocalDate
import com.example.momenttrip.viewmodel.TripViewModel
import com.example.momenttrip.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
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
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }


    // 🔹 구글 로그인 런처 & 콜백 준비
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) {
            userViewModel.isGoogleSignUpPending = false
            errorMessage.value = ""
            FirebaseAuth.getInstance().signOut()
            isLoggedIn.value = FirebaseAuth.getInstance().currentUser == null // ★ 로그아웃 후 동기화
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                userViewModel.signInWithGoogleForSignUp(
                    idToken,
                    onSuccess = {   if (!userViewModel.isGoogleSignUpPending) {
                        isLoggedIn.value = true
                    }},
                    onError = { msg -> errorMessage.value = msg ?: "구글 인증 실패" }
                )
            } else {
                errorMessage.value = "구글 로그인 토큰 획득 실패"
            }
        } catch (e: Exception) {
            errorMessage.value = e.message ?: "구글 로그인 실패"
        }
    }


    // 구글 로그인 인텐트 실행 함수
    fun launchGoogleSignIn() {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        googleLauncher.launch(client.signInIntent)
    }

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
            val trip = tripViewModel.currentTrip.filterNotNull().first()
            Log.d("DEBUG111", "tripState가 유효함: $trip")
            centerTab.value = "currentTrip"
            initialized = true
            tripViewModel.resetTripCreated()
        }
    }

    // 🔹 실제 렌더링 조건
    when {
        // (1) 구글 추가입력 대기
        userViewModel.isGoogleSignUpPending -> {
            GoogleSignUpExtraScreen(
                defaultName = userViewModel.tempGoogleUser?.displayName ?: "",
                onSubmit = { name, nickname, phone ->
                    val firebaseUser = userViewModel.tempGoogleUser
                    if (firebaseUser != null) {
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            login_type = LoginType.GOOGLE,
                            name = name,
                            nickname = nickname,
                            phone_number = phone,
                            profile_url = firebaseUser.photoUrl?.toString(),
                            created_at = com.google.firebase.Timestamp.now()
                        )
                        userViewModel.finalizeGoogleSignUp(user) {
                            userViewModel.isGoogleSignUpPending = false
                            isLoggedIn.value = true
                        }
                    }
                },
                onBack = {
                    // 상태 완전 초기화
                    userViewModel.isGoogleSignUpPending = false
                    userViewModel.tempGoogleUser = null
                    FirebaseAuth.getInstance().signOut()
                    isLoggedIn.value = false

                }
            )
        }
        // 1. 이미 로그인 + 여행 정보까지 준비된 경우 (메인 화면)
        isLoggedIn.value && centerTab.value != null && initialized -> {
            AppNavGraph(
                navController = navController,
                isLoggedIn = true,
                centerTab = centerTab.value!!,
                onLogout = {
                    userViewModel.logout(context) {
                        isLoggedIn.value = false
                    }
                },
                tripViewModel = tripViewModel,
                userViewModel = userViewModel
            )
        }

        // 3. 이메일/비밀번호 회원가입
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
                LoginScreen(
                    email = email.value,
                    password = password.value,
                    errorMessage = errorMessage.value,
                    onEmailChange = { email.value = it },
                    onPasswordChange = { password.value = it },
                    onLoginClick = {
                        Log.d("LoginFlow", "로그인 시도: email=${email.value}")
                        userViewModel.login(
                            email = email.value,
                            password = password.value,
                            callback = { success, message ->
                                if (success) {
                                    Log.d("LoginFlow", "로그인 성공!")
                                    isLoggedIn.value = true
                                } else {
                                    Log.e("LoginFlow", "로그인 실패: $message")
                                    errorMessage.value = message ?: "로그인 실패"
                                }
                            }
                        )
                    },
                    onSignUpClick = { isSignUpMode.value = true },
                    onGoogleLogin = { launchGoogleSignIn() }
                )
            }
        }

        // 4. 로딩 스피너
        else -> {
            AppLoadingScreen()
        }
    }


}


