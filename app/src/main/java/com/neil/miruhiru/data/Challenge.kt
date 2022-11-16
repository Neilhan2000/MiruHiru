package com.neil.miruhiru.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.parcelize.Parceler
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
    var description: String? = null,
    var id: String? = null,
    var image: String? = null,
    var isUpload: Boolean? = null,
    var likeList: List<String>? = null,
    var location:  @RawValue GeoPoint? = null,
    var name: String? = null,
    var stage: Int? = null,
    var timeSpent: Long? = null,
    var totalRating: Float? = null,
    var type: String? = null,
    var completedList: List<String>? = null,
    var commentQuantity: Long? = null,
    var createdTime: Timestamp? = null
) : Parcelable
