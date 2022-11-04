package com.neil.miruhiru.data

import com.google.firebase.firestore.GeoPoint

data class Task(
    val location: GeoPoint = GeoPoint(0.0,0.0),
    val answer: String = "",
    val guide: String = "",
    val id: String = "",
    val image: String = "",
    val introduction: String = "",
    val question: String = "",
    val stage: Int = 0,
    val name: String = ""
)