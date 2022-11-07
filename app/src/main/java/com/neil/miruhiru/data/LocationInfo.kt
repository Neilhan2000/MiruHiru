package com.neil.miruhiru.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationInfo(
    val name: String = "",
    val distance: Int = 0,
    val introduction: String = "",
    val image: String = "",
    val question: String = "",
    val answer: String = ""

) : Parcelable