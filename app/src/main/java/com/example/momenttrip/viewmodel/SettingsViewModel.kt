package com.example.momenttrip.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.User
import com.example.momenttrip.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class SettingsViewModel: ViewModel() {
    // 현재 사용자 정보를 담는 StateFlow
    internal val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    // 로그아웃 상태를 나타내는 StateFlow (로그아웃 성공 시 true)
    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState

    // 닉네임 변경 결과를 나타내는 StateFlow (성공 여부, 에러 메시지)
    private val _nicknameUpdateResult = MutableStateFlow<Pair<Boolean, String?>>(false to null)
    val nicknameUpdateResult: StateFlow<Pair<Boolean, String?>> = _nicknameUpdateResult

    // 프로필 이미지 변경 결과를 나타내는 StateFlow (성공 여부, 에러 메시지)
    private val _profileImageUpdateResult = MutableStateFlow<Pair<Boolean, String?>>(false to null)
    val profileImageUpdateResult: StateFlow<Pair<Boolean, String?>> = _profileImageUpdateResult

    // ViewModel 생성 시 현재 사용자 정보 로드
    init {
        loadCurrentUser()
    }

    // 현재 사용자 정보 불러오기
    fun loadCurrentUser() {
        viewModelScope.launch {
            _user.value = UserRepository.getCurrentUser()
        }
    }

    // 로그아웃 처리
    fun logout() {
        UserRepository.logout()
        _user.value = null
        _logoutState.value = true // 로그아웃 성공 시 true로 변경
    }

    // 닉네임 변경 처리
    fun updateNickname(newNickname: String) {
        viewModelScope.launch {
            val result = UserRepository.updateNickname(newNickname)
            if (result.isSuccess) {
                // 성공 시 ViewModel 내 상태도 업데이트
                _user.value = _user.value?.copy(nickname = newNickname)
                _nicknameUpdateResult.value = true to null
            } else {
                // 실패 시 에러 메시지 전달
                _nicknameUpdateResult.value = false to (result.exceptionOrNull()?.message)
            }
        }
    }

    // 프로필 이미지 변경 처리
    fun updateProfileImage(imageUri: Uri) {
        val uid = _user.value?.uid ?: return // 로그인 상태가 아니면 리턴
        viewModelScope.launch {
            // 1. 기존 이미지 삭제
            UserRepository.deleteOldProfileImage(uid)
            // 2. 새 이미지 업로드
            val uploadResult = UserRepository.uploadProfileImageFile(uid, imageUri)
            if (uploadResult.isSuccess) {
                val url = uploadResult.getOrNull()
                if (!url.isNullOrBlank()) {
                    // 3. Firestore에 URL 저장
                    val saveResult = UserRepository.updateProfileImage(url)
                    if (saveResult.isSuccess) {
                        // 4. ViewModel 내 상태도 업데이트
                        _user.value = _user.value?.copy(profile_url = url)
                        _profileImageUpdateResult.value = true to null
                    } else {
                        _profileImageUpdateResult.value = false to (saveResult.exceptionOrNull()?.message)
                    }
                } else {
                    _profileImageUpdateResult.value = false to "URL 생성 실패"
                }
            } else {
                _profileImageUpdateResult.value = false to (uploadResult.exceptionOrNull()?.message)
            }
        }
    }
}