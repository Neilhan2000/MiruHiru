package com.neil.miruhiru.data.source.local

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson

class MiruHiruTypeConverters {

    @TypeConverter
    fun stringToGeoPoint(data: String?): GeoPoint? {
        return Gson().fromJson(data, GeoPoint::class.java)
    }

    @TypeConverter
    fun geoPointToString(geoPoint: GeoPoint?): String? {
        return Gson().toJson(geoPoint)
    }
}