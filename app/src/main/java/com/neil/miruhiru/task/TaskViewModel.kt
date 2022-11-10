package com.neil.miruhiru.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Task
import timber.log.Timber

class TaskViewModel(application: Application): AndroidViewModel(application) {

    val viewModelApplication = application

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>>
        get() = _taskList

    private val _annotationList = MutableLiveData<List<Task>>()
    val annotationList: LiveData<List<Task>>
        get() = _annotationList

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    var currentStage = -1
    var totalStage = -1

    fun loadEventsWithTask(challengeDocumentId: String, eventId: String) {
        val db = Firebase.firestore
        val taskList = mutableListOf<Task>()
        val annotationList = mutableListOf<Task>()

        Timber.i("challenge document id $challengeDocumentId, event id $eventId")
        db.collection("events").whereEqualTo("id" , eventId)
            .get()
            .addOnSuccessListener { result ->
                Timber.i("result size ${result.documents.size}")
                Timber.i("get document success")
                for (document in result) {
                    Timber.i("go into for loop")
                    val event = document.toObject<Event>()
                    _event.value = event
                    currentStage = event.progress.minOrNull() ?: -1
                    totalStage = event.stage

                    Timber.i("task list event progress ${event.progress}")
                    Timber.i("current stage $currentStage")


                    db.collection("challenges").document(challengeDocumentId)
                        .collection("tasks").orderBy("stage", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { result ->
                            Timber.i("challenge document id = $challengeDocumentId, result ${result.documents.size}")
                            for (document in result) {
                                val task = document.toObject<Task>()
                                if (task.stage <= currentStage) {
                                    taskList.add(task)
                                }
                                annotationList.add(task)
                            }
                            Timber.i("task list size ${taskList.size} current stage$currentStage")
                            _taskList.value = taskList
                            _annotationList.value = annotationList
                        }
                        .addOnFailureListener { exception ->
                            Timber.i(exception, "Error getting documents.")
                        }
                }


            }
            .addOnFailureListener { exception ->
                Timber.tag("neil").i(exception, "Error getting documents.")
            }

    }

    private fun addTask(challengeId: String) {
        val user = hashMapOf(
            "answer" to "Ada",
            "guide" to "Lovelace",
            "id" to "1815",
            "image" to "",
            "introduction" to "",
            "location" to GeoPoint(0.0, 0.0),
            "name" to "",
            "question" to "",
            "stage" to -1
        )

        val db = Firebase.firestore
        db.collection("challenges").document(challengeId)
            .collection("tasks")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Timber.tag("neil").d("DocumentSnapshot added with ID: %s", documentReference.id)
            }
            .addOnFailureListener { e ->
                Timber.tag("neil").w(e, "Error adding document")
            }
    }

//    private fun calculateDistance(currentPoint: Point?, destinationPoint: GeoPoint): Float {
//        val current = Location("current")
//        current.latitude = currentPoint?.latitude()!!
//        current.longitude = currentPoint.latitude()
//
//        val destination = Location("current")
//        destination.latitude = destinationPoint.latitude
//        destination.longitude = destinationPoint.longitude
//
//        val result =  floatArrayOf(0.0F)
//        val distance = Location.distanceBetween(currentPoint.latitude(), currentPoint.longitude(),
//            destinationPoint.latitude, destinationPoint.longitude, result)
//
//        return result[0]
//
//    }
//
//    @SuppressLint("MissingPermission")
//    fun calculateAndShowDistance(destination: GeoPoint) {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(viewModelApplication)
//        var currentPoint: Point? = null
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location : Location? ->
//                Log.i("neil", "current location = ${location?.latitude}, ${location?.longitude}")
//                currentPoint = Point.fromLngLat(
//                    location!!.longitude,
//                    location!!.latitude
//                )
//                val distance = calculateDistance(currentPoint, destination)
//                Log.i("neil", "time $distance")
//            }
//    }
}