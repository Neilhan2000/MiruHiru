package com.neil.miruhiru.data

import com.google.firebase.Timestamp
import java.util.*

data class Log(
    val photo: String = "",
    val text: String = "",
    val time: Timestamp = Timestamp(Date(0)),
    val stage: Int = -1,
    val senderId: String = ""
)
