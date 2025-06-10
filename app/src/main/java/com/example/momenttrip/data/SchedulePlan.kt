package com.example.momenttrip.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class SchedulePlan(
    val title: String = "",
    val content: String = "",
    val start_time: String = "", // ex: "14:00"
    val end_time: String = "",
    val author_uid: String = "",
    val created_at: Timestamp = Timestamp.now(),
    @get:Exclude val documentId: String = ""    // 추가!

)
