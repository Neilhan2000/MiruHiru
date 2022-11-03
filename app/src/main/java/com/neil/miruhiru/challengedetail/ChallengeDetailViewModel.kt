package com.neil.miruhiru.challengedetail

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
import com.neil.miruhiru.data.Task

class ChallengeDetailViewModel : ViewModel() {

    init {
        loadChallenge()
        loadTasks()
    }

    private val _challenge = MutableLiveData<Challenge>()
    val challenge: LiveData<Challenge>
        get() = _challenge

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>>
        get() = _taskList

    private fun loadChallenge() {
        val db = Firebase.firestore

        db.collection("challenges").document("2WBySSd68w3VrA08eLGj")
            .get()
            .addOnSuccessListener { result ->
                val challenge = result.toObject<Challenge>()
                _challenge.value = challenge!!

//                Log.i("neil", "success load documents = ${_challenge.value}")
            }
            .addOnFailureListener { exception ->
                Log.i("neil", "Error getting documents.", exception)
            }

    }

    private fun loadTasks() {
        val db = Firebase.firestore
        val taskList = mutableListOf<Task>()

        db.collection("challenges").document("2WBySSd68w3VrA08eLGj")
            .collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val task = document.toObject<Task>()
                    taskList.add(task)
                }
                _taskList.value = taskList
                Log.i("neil", "success load documents = ${taskList}")
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