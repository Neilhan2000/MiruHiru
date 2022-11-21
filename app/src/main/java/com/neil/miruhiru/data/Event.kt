package com.neil.miruhiru.data

import com.google.firebase.Timestamp
import java.util.*

data class Event(
    val id: String = "",
    val members: List<String> = mutableListOf(""),
    val startTime: Timestamp = Timestamp(Date(0)),
    val isCompleted: Boolean = false,
    val challengeId: String = "",
    val progress: List<Int> = mutableListOf(0),
    val stage: Int = 0,
    val status: String = "",
    val endTime: Timestamp = Timestamp(Date(0)),
    val currentMembers: List<String> = listOf(),
    val personal: Boolean = false
)

