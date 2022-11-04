package com.neil.miruhiru.task

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
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Task

class TaskViewModel: ViewModel() {

    init {
        val challengeId = "2WBySSd68w3VrA08eLGj"
//        loadTasks(challengeId)
//        loadEvents()
        loadEventsWithTask(challengeId)
    }

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>>
        get() = _taskList

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

//    private fun loadTasks(challengeId: String) {
//        val db = Firebase.firestore
//        val taskList = mutableListOf<Task>()
//
//        db.collection("challenges").document(challengeId)
//            .collection("tasks")
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val task = document.toObject<Task>()
//                    taskList.add(task)
//                }
//                _taskList.value = taskList
////                Log.i("neil", "success load documents = ${taskList}")
//            }
//            .addOnFailureListener { exception ->
//                Log.i("neil", "Error getting documents.", exception)
//            }
//
//    }
//
//    private fun loadEvents() {
//        val db = Firebase.firestore
//
//        db.collection("events").whereEqualTo("id" ,"0")
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val event = document.toObject<Event>()
//                    _event.value = event
//                }
//                Log.i("neil", "success load documents = ${event.value}")
//            }
//            .addOnFailureListener { exception ->
//                Log.i("neil", "Error getting documents.", exception)
//            }
//
//    }

    private fun loadEventsWithTask(challengeId: String) {
        val db = Firebase.firestore
        val taskList = mutableListOf<Task>()

        db.collection("events").whereEqualTo("id" ,"0")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val event = document.toObject<Event>()
                    _event.value = event
                }

                Log.i("neil", "success load documents = ${event.value}")
                db.collection("challenges").document(challengeId)
                    .collection("tasks")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val task = document.toObject<Task>()
                            taskList.add(task)
                        }
                        _taskList.value = taskList
//                Log.i("neil", "success load documents = ${taskList}")
                    }
                    .addOnFailureListener { exception ->
                        Log.i("neil", "Error getting documents.", exception)
                    }
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