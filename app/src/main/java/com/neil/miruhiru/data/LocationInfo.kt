package com.neil.miruhiru.data

import com.google.firebase.firestore.GeoPoint

data class LocationInfo(
    val name: String,
    val location: GeoPoint,
    val stage: Int,
    val totalRating: Float,
    val timeSpent: Long,
    val image: String

)