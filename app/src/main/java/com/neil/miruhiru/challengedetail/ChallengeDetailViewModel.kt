package com.neil.miruhiru.challengedetail

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mapbox.geojson.Point
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.*
import timber.log.Timber

class ChallengeDetailViewModel(challengeId: String) : ViewModel() {

    init {
        loadChallenge(challengeId)
    }

    private val _challenge = MutableLiveData<Challenge>()
    val challenge: LiveData<Challenge>
        get() = _challenge

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>>
        get() = _taskList

    private val _commentList = MutableLiveData<List<Comment>>()
    val commentList: LiveData<List<Comment>>
        get() = _commentList

    private val _commentUsers = MutableLiveData<List<User>>()
    val commentUsers: LiveData<List<User>>
        get() = _commentUsers

    private val _hasCurrentEvent = MutableLiveData<Boolean>()
    val hasCurrentEvent: LiveData<Boolean>
        get() = _hasCurrentEvent

    fun checkHasCurrentEvent(challengeId: String) {
        val db = Firebase.firestore

        if (UserManager.user.currentEvent.isNotEmpty()) {
            db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
                .get()
                .addOnSuccessListener {
                    val event = it.documents[0].toObject<Event>()
                    _hasCurrentEvent.value = event?.challengeId == challengeId
                }
        } else {
            _hasCurrentEvent.value = false
        }
    }

    fun cleanEventSingle() {
        val db = Firebase.firestore
        var userDocumented = ""

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                userDocumented = it.documents[0].id

                db.collection("users").document(userDocumented)
                    .update("currentEvent", "")
                UserManager.getUser()
            }
    }







    private var challengeDocumentId = ""

    private fun loadChallenge(challengeId: String) {
        val db = Firebase.firestore

        db.collection("challenges").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener { result ->
                val challenge = result.documents[0].toObject<Challenge>()
                _challenge.value = challenge!!
                challengeDocumentId = result.documents[0].id

                loadTasks()

                Timber.tag("neil").i("success load documents = %s", _challenge.value)
            }
            .addOnFailureListener { exception ->
                Timber.tag("neil").i(exception, "Error getting documents.")
            }

    }

    private fun loadTasks() {
        val db = Firebase.firestore
        val taskList = mutableListOf<Task>()

        db.collection("challenges").document(challengeDocumentId)
            .collection("tasks")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val task = document.toObject<Task>()
                    taskList.add(task)
                }
                _taskList.value = taskList
                Timber.i("success load documents = %s", taskList)
            }
            .addOnFailureListener { exception ->
                Timber.i(exception, "Error getting documents.")
            }

    }

    fun loadComments() {
        val db = Firebase.firestore
        val commentList = mutableListOf<Comment>()

        db.collection("challenges").document(challengeDocumentId)
            .collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val commentUsers = mutableListOf<User>()

                for (document in result) {
                    val comment = document.toObject<Comment>()
                    commentList.add(comment)

                    // add user to comment user list
                    db.collection("users").whereEqualTo("id", comment.userId)
                        .get().addOnSuccessListener {
                            val user = it.documents[0].toObject<User>()
                            if (user != null) {
                                commentUsers.add(user)
                                _commentUsers.value = commentUsers
                                _commentList.value = commentList
                            }

                        }

                }

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