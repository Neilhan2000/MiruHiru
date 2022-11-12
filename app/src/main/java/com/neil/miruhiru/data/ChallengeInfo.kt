package com.neil.miruhiru.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChallengeInfo(
    val id: String? = null,
    val stage: Int? = null
) : Parcelable
