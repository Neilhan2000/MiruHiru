package com.neil.miruhiru.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.firestore.GeoPoint
import com.neil.miruhiru.data.source.local.MiruHiruTypeConverters
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.Parceler

@Entity(tableName = "task_table", primaryKeys = ["task_id"])
@TypeConverters(MiruHiruTypeConverters::class)
@Parcelize
data class Task(
    @ColumnInfo(name = "location")
    var location: GeoPoint = GeoPoint(0.0,0.0),
    @ColumnInfo(name = "answer")
    var answer: String = "",
    @ColumnInfo(name = "guide")
    var guide: String = "",
    @ColumnInfo(name = "task_id")
    var id: String = "",
    @ColumnInfo(name = "image")
    var image: String = "",
    @ColumnInfo(name = "introduction")
    var introduction: String = "",
    @ColumnInfo(name = "question")
    var question: String = "",
    @ColumnInfo(name = "stage")
    var stage: Int = 0,
    @ColumnInfo(name = "name")
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