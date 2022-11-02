package com.neil.miruhiru.explore

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mapbox.geojson.Point
import com.neil.miruhiru.data.Challenge

class ExploreViewModel: ViewModel() {

    init {
        loadChallenges()
    }

    private val _challengeList = MutableLiveData<List<Challenge>>()
    val challengeList: LiveData<List<Challenge>>
        get() = _challengeList

    private fun loadChallenges() {
        val db = Firebase.firestore
        val challengeList = mutableListOf<Challenge>()

        db.collection("challenges")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val challenge = document.toObject<Challenge>()
                    challengeList.add(challenge)
                }
                _challengeList.value = challengeList
                Log.i("neil", "success load article = ${challengeList}")
                Log.i("neil", "success load article = ${result.documents[0].data}")
            }
            .addOnFailureListener { exception ->
                Log.i("neil", "Error getting documents.", exception)
            }

    }

    fun calculateDistance(currentPoint: Point?, destinationPoint: GeoPoint): Float {
        val current = Location("current")
        current.latitude = currentPoint?.latitude()!!
        current.longitude = currentPoint.latitude()

        val destination = Location("current")
        destination.latitude = destinationPoint.latitude
        destination.longitude = destinationPoint.longitude

        val result =  floatArrayOf(0.0F)
        val distance = Location.distanceBetween(currentPoint.latitude(), currentPoint.longitude(),
        destinationPoint.latitude, destinationPoint.longitude, result)

        return result[0]

    }
}