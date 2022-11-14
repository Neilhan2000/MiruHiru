package com.neil.miruhiru.data

import com.google.firebase.Timestamp
import java.util.*

data class Message(
    val senderId: String = "",
    val text: String = "",
    val time: Timestamp = Timestamp(Date(0))
)