package com.neil.miruhiru.data

import com.google.firebase.Timestamp
import java.util.*

data class Photo(
    val id: String = "",
    val url: String = "",
    val time: Timestamp = Timestamp(Date(0)),
    val comments: List<PhotoComment> = mutableListOf()
)