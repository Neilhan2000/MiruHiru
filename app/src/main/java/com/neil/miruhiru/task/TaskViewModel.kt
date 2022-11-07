package com.neil.miruhiru.task

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.data.Task
import kotlinx.coroutines.*

class TaskViewModel(application: Application): AndroidViewModel(application) {

    val viewModelApplication = application
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

    val currentStage = 2

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