package com.example.momenttrip.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.User
import com.example.momenttrip.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val uid: String? = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadUserProfile()
    }

    //유저 정보 불러오기
    fun loadUserProfile() {
        viewModelScope.launch {
            _user.value = UserRepository.getCurrentUser()
        }
    }

    //유저 이름 변경
    fun updateUserName(newName: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = UserRepository.updateNickname(newName)
            if (result.isSuccess) {
                _user.value = _user.value?.copy(nickname = newName)
                callback(true, null)
            } else {
                callback(false, result.exceptionOrNull()?.message)
            }
        }
    }

    //프로필 이미지 변경
    fun updateProfileImage(imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val uid = uid ?: return callback(false, "로그인 필요")

        viewModelScope.launch {
            // 이전 이미지 삭제
            UserRepository.deleteOldProfileImage(uid)
            // 새 이미지 업로드
            val uploadResult = UserRepository.uploadProfileImageFile(uid, imageUri)
            if (uploadResult.isSuccess) {
                val url = uploadResult.getOrNull()
                if (!url.isNullOrBlank()) {
                    val saveResult = UserRepository.updateProfileImage(url)
                    if (saveResult.isSuccess) {
                        _user.value = _user.value?.copy(profile_url = url)
                        callback(true, null)
                    } else {
                        callback(false, saveResult.exceptionOrNull()?.message)
                    }
                } else {
                    callback(false, "URL 생성 실패")
                }
            } else {
                callback(false, uploadResult.exceptionOrNull()?.message)
            }
        }
    }

    //로그아웃
    fun logout() {
        UserRepository.logout()
        _user.value = null
    }
}
