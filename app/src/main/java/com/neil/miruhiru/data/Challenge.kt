package com.neil.miruhiru.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.parcelize.Parceler
import java.util.*
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
    var description: String = "",
    var id: String = "",
    var image: String = "",
    var isUpload: Boolean = false,
    var likeList: List<String> = listOf(),
    var location:  @RawValue GeoPoint = GeoPoint(0.0, 0.0),
    var name: String = "",
    var stage: Int = -1,
    var timeSpent: Long = -1,
    var totalRating: Float = 0F,
    var type: String = "",
    var completedList: List<String> = listOf(),
    var commentQuantity: Long = -1,
    var createdTime: Timestamp = Timestamp(Date(0))
) : Parcelable
