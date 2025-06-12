package com.example.momenttrip.repository

import com.example.momenttrip.data.TripInviteEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object TripInviteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 1. 여행 초대 전송
    suspend fun sendInvite(entry: TripInviteEntry): Result<String> {
        return try {
            val docRef = db.collection("trip_invites").add(entry).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. 특정 유저가 받은 초대 목록 불러오기
    suspend fun getInvitesForUser(uid: String): Result<List<TripInviteEntry>> {
        return try {
            val snapshot = db.collection("trip_invites")
                .whereEqualTo("to_uid", uid)
                .get()
                .await()
            val invites = snapshot.documents.mapNotNull {
                it.toObject(TripInviteEntry::class.java)?.copy(invite_id = it.id)
            }
            Result.success(invites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. 초대 수락 처리: trips에 참여자로 추가 + 유저 current_trip_id 업데이트
    suspend fun acceptInvite(invite: TripInviteEntry): Result<Unit> {
        return try {
            val batch = db.batch()
            val tripRef = db.collection("trips").document(invite.trip_id)
            val userRef = db.collection("users").document(invite.to_uid)
            val inviteRef = db.collection("trip_invites").document(invite.invite_id!!)

            // 여행에 participants 배열에 유저 UID 추가
            batch.update(tripRef, "owner_uids", com.google.firebase.firestore.FieldValue.arrayUnion(invite.to_uid))
            // 유저의 current_trip_id 업데이트
            batch.update(userRef, "current_trip_id", invite.trip_id)
            // 초대 삭제
            batch.delete(inviteRef)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. 초대 거절 처리: 초대 삭제
    suspend fun declineInvite(inviteId: String): Result<Unit> {
        return try {
            db.collection("trip_invites").document(inviteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun incomingInvitesFlow(): Flow<List<TripInviteEntry>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reg = Firebase.firestore
            .collection("trip_invites")
            .whereEqualTo("to_uid", uid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.toObjects(TripInviteEntry::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { reg.remove() }
    }
}
