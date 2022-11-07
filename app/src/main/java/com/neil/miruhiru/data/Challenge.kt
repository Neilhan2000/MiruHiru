package com.neil.miruhiru.data

import com.google.firebase.firestore.GeoPoint

data class Challenge(
    val description: String? = null,
    val id: String? = null,
    val image: String? = null,
    val isUpload: Boolean? = null,
    val likeList: List<String>? = null,
    val location: GeoPoint? = null,
    val name: String? = null,
    val stage: Int? = null,
    val timeSpent: Long? = null,
    val totalRating: Float? = null,
    val type: String? = null
)
