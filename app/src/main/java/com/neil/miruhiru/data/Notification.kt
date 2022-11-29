package com.neil.miruhiru.data

import com.google.firebase.Timestamp
import java.util.*

data class Notification(
    val title: String = "",
    val content: String = "",
    val sendTime: Timestamp = Timestamp(Date(0))
)
