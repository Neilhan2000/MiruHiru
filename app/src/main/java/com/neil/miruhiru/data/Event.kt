package com.neil.miruhiru.data

import com.google.firebase.Timestamp
import java.util.*

data class Event(
    val id: String = "",
    val members: List<String> = mutableListOf(""),
    val startTime: Timestamp = Timestamp(Date(0)),
    val isComplete: Boolean = false,
    val challengeId: String = "",
    val progress: List<Int> = mutableListOf(0),
    val messages: List<Message> = mutableListOf(),
    val photos: List<Photo> = mutableListOf()
)

data class Message(
    val senderId: String = "",
    val text: String = "",
    val time: Timestamp = Timestamp(Date(0))
)

data class Photo(
    val id: String = "",
    val url: String = "",
    val time: Timestamp = Timestamp(Date(0)),
    val comments: List<PhotoComment> = mutableListOf()
)

data class PhotoComment(
    val senderId: String = "",
    val text: String = "",
    val time: Timestamp = Timestamp(Date(0))
)

