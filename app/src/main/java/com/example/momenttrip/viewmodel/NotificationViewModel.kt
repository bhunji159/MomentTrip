package com.example.momenttrip.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.TripInviteEntry
import com.example.momenttrip.repository.FriendRepository
import com.example.momenttrip.repository.TripInviteRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 알림 항목 모델 정의 */
sealed interface NotificationItem {
    val id: String
    val fromName: String
    val createdAt: Timestamp

    data class FriendReq(
        override val id: String,
        override val fromName: String,
        override val createdAt: Timestamp,
        val fromUid: String
    ) : NotificationItem

    data class TripInvite(
        override val id: String,
        override val fromName: String,
        override val createdAt: Timestamp,
        val tripId: String
    ) : NotificationItem
}

/**
 * 알림 ViewModel
 * DI 없이 기본 싱글턴 레포지토리 사용
 */
class NotificationViewModel(
    private val friendRepo: FriendRepository = FriendRepository,
    private val inviteRepo: TripInviteRepository = TripInviteRepository
) : ViewModel() {

    // 친구 요청 스트림 변환
    private val friendFlow = friendRepo.incomingRequestsFlow()
        .map { reqs ->
            reqs.map {
                NotificationItem.FriendReq(
                    id = it.request_id!!,
                    fromName  = it.from_name.ifBlank { it.from_uid },
                    createdAt = it.created_at,
                    fromUid = it.from_uid
                )
            }
        }

    // 여행 초대 스트림 변환
    private val inviteFlow = inviteRepo.incomingInvitesFlow()
        .map { invs ->
            invs.map {
                NotificationItem.TripInvite(
                    id = it.invite_id!!,
                    fromName = it.from_uid,
                    createdAt = it.created_at,
                    tripId = it.trip_id
                )
            }
        }

    /** 모든 알림 (시간 내림차순) */
    val notifications: StateFlow<List<NotificationItem>> = combine(friendFlow, inviteFlow) { f, i ->
        (f + i).sortedByDescending { it.createdAt }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 알림 뱃지 표시 여부 */
    val hasPending: StateFlow<Boolean> =
        notifications.map { it.isNotEmpty() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** 수락 */
    fun accept(item: NotificationItem) = viewModelScope.launch {
        when (item) {
            is NotificationItem.FriendReq ->
                friendRepo.acceptFriendRequest(item.id, item.fromUid)

            is NotificationItem.TripInvite -> {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                inviteRepo.acceptInvite(
                    TripInviteEntry(
                        invite_id = item.id,
                        trip_id = item.tripId,
                        to_uid = uid,
                        from_uid = item.fromName,
                        created_at = item.createdAt
                    )
                )
            }
        }
    }

    /** 거절 */
    fun reject(item: NotificationItem) = viewModelScope.launch {
        when (item) {
            is NotificationItem.FriendReq ->
                friendRepo.declineFriendRequest(item.id, item.fromUid)

            is NotificationItem.TripInvite ->
                inviteRepo.declineInvite(item.id)
        }
    }
}
