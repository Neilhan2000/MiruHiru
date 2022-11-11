package com.neil.miruhiru.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

data class ParcelGeoPoint(
    val geoPoint: GeoPoint
) : Parcelable {


    constructor(parcel: Parcel) : this(
        GeoPoint(0.0, 0.0)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelGeoPoint> {
        override fun createFromParcel(parcel: Parcel): ParcelGeoPoint {
            return ParcelGeoPoint(parcel)
        }

        override fun newArray(size: Int): Array<ParcelGeoPoint?> {
            return arrayOfNulls(size)
        }
    }
}

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
