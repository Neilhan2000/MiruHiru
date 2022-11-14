package com.neil.miruhiru.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlin.math.ln

class ParcelableGeoPoint() : Parcelable {

    private lateinit var geoPoint: GeoPoint

    constructor(parcel: Parcel) : this() {
        parcel.writeDouble(geoPoint.latitude)
        parcel.writeDouble(geoPoint.longitude)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val lat = parcel.readDouble()
        val lng = parcel.readDouble()
        geoPoint = GeoPoint(lat, lng)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableGeoPoint> {
        override fun createFromParcel(parcel: Parcel): ParcelableGeoPoint {
            return ParcelableGeoPoint(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableGeoPoint?> {
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
    val location:  @RawValue GeoPoint? = null,
    val name: String? = null,
    val stage: Int? = null,
    val timeSpent: Long? = null,
    val totalRating: Float? = null,
    val type: String? = null,
    val completedList: List<String>? = null,
    val commentQuantity: Long? = null
) : Parcelable
