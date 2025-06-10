//import android.app.Activity
//import android.content.Context
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.momenttrip.R
//import com.example.momenttrip.ui.screen.login.LoginScreen
//import com.example.momenttrip.ui.screen.login.LoginViewModel
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.ApiException
//
//@Composable
//fun LoginEntryPoint(
//    onLoginSuccess: () -> Unit,
//    onSignUpClick: () -> Unit
//) {
//    val viewModel: LoginViewModel = viewModel()
//    val errorMessage = remember { mutableStateOf("") }
//    val context = androidx.compose.ui.platform.LocalContext.current
//
//    // **1. 구글 로그인 런처**
//    val googleLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        try {
//            val account = task.getResult(ApiException::class.java)
//            val idToken = account?.idToken
//            if (idToken != null) {
//                // LoginViewModel에 signInWithGoogle 함수 추가 필요!
//                viewModel.signInWithGoogle(
//                    idToken,
//                    onSuccess = {
//                        errorMessage.value = ""
//                        onLoginSuccess()
//                    },
//                    onError = { msg ->
//                        errorMessage.value = msg ?: "구글 인증 실패"
//                    }
//                )
//            } else {
//                errorMessage.value = "구글 로그인 토큰 획득 실패"
//            }
//        } catch (e: Exception) {
//            errorMessage.value = e.message ?: "구글 로그인 실패"
//        }
//    }
//
//    // **2. 인텐트 실행 함수**
//    fun launchGoogleSignIn() {
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(context.getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        val client = GoogleSignIn.getClient(context, gso)
//        googleLauncher.launch(client.signInIntent)
//    }
//
//    LoginScreen(
//        email = viewModel.email.value,
//        password = viewModel.password.value,
//        errorMessage = errorMessage.value,
//        onEmailChange = { viewModel.email.value = it },
//        onPasswordChange = { viewModel.password.value = it },
//        onLoginClick = {
//            viewModel.login(
//                email = viewModel.email.value,
//                password = viewModel.password.value,
//                onSuccess = {
//                    errorMessage.value = ""
//                    onLoginSuccess()
//                },
//                onError = { msg ->
//                    errorMessage.value = msg
//                }
//            )
//        },
//        onSignUpClick = onSignUpClick,
//        onGoogleLogin = { launchGoogleSignIn() } // **구글 로그인 이벤트 연결**
//    )
//}
