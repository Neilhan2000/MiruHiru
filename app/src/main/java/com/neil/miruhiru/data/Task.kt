package com.neil.miruhiru.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.Parceler

@Parcelize
data class Task(
    var location: GeoPoint = GeoPoint(0.0,0.0),
    var answer: String = "",
    var guide: String = "",
    var id: String = "",
    var image: String = "",
    var introduction: String = "",
    var question: String = "",
    var stage: Int = 0,
    var name: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        GeoPoint(0.0, 0.0),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
    )

    companion object : Parceler<Task> {

        override fun Task.write(parcel: Parcel, flags: Int) {
            parcel.writeDouble(location.latitude)
            parcel.writeDouble(location.longitude)
            parcel.writeString(answer)
            parcel.writeString(guide)
            parcel.writeString(id)
            parcel.writeString(image)
            parcel.writeString(introduction)
            parcel.writeString(question)
            parcel.writeInt(stage)
            parcel.writeString(name)
        }

        override fun create(parcel: Parcel): Task {
            return Task(
                GeoPoint(parcel.readDouble(), parcel.readDouble()),
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readInt(),
                parcel.readString() ?: ""

            )
        }
    }
}