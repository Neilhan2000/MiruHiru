package com.neil.miruhiru.data

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Challenge(
    val description: String? = null,
    val id: String? = null,
    val image: String? = null,
    val isUpload: Boolean? = null,
    val likeList: List<String>? = null,
    val location: @RawValue GeoPoint? = null,
    val name: String? = null,
    val stage: Int? = null,
    val timeSpent: Long? = null,
    val totalRating: Float? = null,
    val type: String? = null,
    val completedList: List<String>? = null
) : Parcelable
