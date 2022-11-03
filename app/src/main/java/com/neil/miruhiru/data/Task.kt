package com.neil.miruhiru.data

import com.google.firebase.firestore.GeoPoint

data class Task(
    val location: GeoPoint? = null,
    val answer: String? = null,
    val guide: String? = null,
    val id: String? = null,
    val image: String? = null,
    val introduction: String? = null,
    val question: String? = null,
    val stage: Int? = null
)