package com.example.momenttrip.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.momenttrip.data.LoginType
import com.example.momenttrip.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object UserRepository{
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    //현재 사용자
    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = db.collection("users").document(uid).get().await()
        Log.d("UserRepository", "snapshot data: ${snapshot.data}")
        val user = snapshot.toObject(User::class.java)
        Log.d("UserRepository", "parsed user: $user")
        return snapshot.toObject(User::class.java)?.copy(uid = uid)
    }

    //회원 가입
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        nickname: String,
        phoneNumber: String
    ): Result<Unit> {
        return try {
            // 1. Firebase Authentication에 계정 생성
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("회원가입 실패: UID 없음"))

            // 2. User 객체 생성
            val user = User(
                uid = uid,
                email = email,
//                password = password, // 보안상 Firestore에는 저장하지 않는 게 일반적. 필요시 제거 가능
                login_type = LoginType.EMAIL,
                name = name,
                nickname = nickname,
                phone_number = phoneNumber,
                profile_url = null,
                created_at = com.google.firebase.Timestamp.now(),
                search_key = listOf(nickname, email, phoneNumber), //닉네임과 이메일, 전화번호를 검색 키워드로
                friends = emptyList(),
                friend_request_ids = emptyList(),
                trip_invite_ids = emptyList()
            )

            // 3. Firestore에 유저 정보 저장
            db.collection("users").document(uid).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //로그인
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //로그인 유지
    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    //로그아웃
    fun logout(context: Context, onDone: () -> Unit = {}) {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            onDone()
        }
    }

    //이메일 중복 체크
    suspend fun isEmailTaken(email: String): Boolean {
        val result = db.collection("users")
            .whereEqualTo("email", email)
            .get().await()
        return !result.isEmpty
    }

    //전화번호 중복 체크
    suspend fun isPhoneNumberTaken(phoneNumber: String): Boolean {
        val result = db.collection("users")
            .whereEqualTo("phone_number", phoneNumber)
            .get().await()
        return !result.isEmpty
    }

    //닉네임 중복 체크: 회원가입 시
    suspend fun isNicknameTakenForSignup(nickname: String): Boolean {
        val result = db.collection("users")
            .whereEqualTo("nickname", nickname)
            .get().await()
        return !result.isEmpty
    }

    //닉네임 중복 체크: 프로필 수정 시
    suspend fun isNicknameTakenForUpdate(nickname: String): Boolean {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return true
        val result = db.collection("users")
            .whereEqualTo("nickname", nickname)
            .get().await()
        return result.any { it.id != uid }
    }

    //닉네임 변경
    suspend fun updateNickname(nickname: String): Result<Unit> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("로그인 필요"))

        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("nickname", nickname)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    //프로필 이미지 업로드
    suspend fun uploadProfileImageFile(uid: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("images/users/$uid/profile.jpg")

            // 이미지 업로드
            storageRef.putFile(imageUri).await()

            // 다운로드 URL 가져오기
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //프로필 이미지 변경: 이미지 url을 firebase에 저장
    suspend fun updateProfileImage(imageUrl: String): Result<Unit> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("로그인 필요"))

        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("profile_url", imageUrl)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //기존 프로필 이미지 삭제
    suspend fun deleteOldProfileImage(uid: String): Result<Unit> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("images/users/$uid/profile.jpg")

            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("UserRepository", "이전 프로필 이미지 삭제 실패 (무시): ${e.message}")
            Result.success(Unit)
        }
    }
    //여행 종료 로직
    suspend fun finishTrip(userId: String, tripId: String): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)

                val rawList = snapshot.get("past_trips")
                val pastTrips = if (rawList is List<*>) {
                    rawList.filterIsInstance<String>()
                } else {
                    emptyList()
                }

                if (!pastTrips.contains(tripId)) {
                    val updatedTrips = pastTrips + tripId
                    transaction.update(userRef, mapOf(
                        "past_trips" to updatedTrips,
                        "current_trip_id" to null
                    ))
                } else {
                    transaction.update(userRef, "current_trip_id", null)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateCurrentTripId(userId: String, tripId: String) {
        db.collection("users")
            .document(userId)
            .update("current_trip_id", tripId)
            .await()
    }

    suspend fun saveUserIfNotExists(user: User) {
        val doc = db.collection("users").document(user.uid).get().await()
        if (!doc.exists()) {
            db.collection("users").document(user.uid).set(user).await()
        }
        // 이미 있으면 아무것도 안 함
    }
    suspend fun saveUser(user: User) {
        db.collection("users").document(user.uid).set(user).await()
    }

}