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
import com.neil.miruhiru.data.Comment
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.data.User
import timber.log.Timber

class ChallengeDetailViewModel : ViewModel() {

    init {
        val challengeId = "2WBySSd68w3VrA08eLGj"
        loadChallenge(challengeId)
        loadTasks(challengeId)
//        loadComments(challengeId)
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

    lateinit var user: User

    private val _commentUsers = MutableLiveData<List<User>>()
    val commentUsers: LiveData<List<User>>
        get() = _commentUsers

    private fun loadChallenge(challengeId: String) {
        val db = Firebase.firestore

        db.collection("challenges").document(challengeId)
            .get()
            .addOnSuccessListener { result ->
                val challenge = result.toObject<Challenge>()
                _challenge.value = challenge!!

                Timber.tag("neil").i("success load documents = %s", _challenge.value)
            }
            .addOnFailureListener { exception ->
                Timber.tag("neil").i(exception, "Error getting documents.")
            }

    }

    private fun loadTasks(challengeId: String) {
        val db = Firebase.firestore
        val taskList = mutableListOf<Task>()

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
                Timber.tag("neil").i(exception, "Error getting documents.")
            }

    }

    fun loadComments(challengeId: String) {
        val db = Firebase.firestore
        val commentList = mutableListOf<Comment>()

        db.collection("challenges").document(challengeId)
            .collection("comments")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val comment = document.toObject<Comment>()
                    commentList.add(comment)
                    addUserToCommentUsersList(comment.userId)
                }
                _commentList.value = commentList
                Timber.tag("neil").i("success load documents = %s", commentList)
            }
            .addOnFailureListener { exception ->
                Timber.tag("neil").i(exception, "Error getting documents.")
            }

    }

    private fun addUserToCommentUsersList(userId: String) {
        val db = Firebase.firestore
        val commentUsers = mutableListOf<User>()
        db.collection("users")
            .whereEqualTo("id", userId)
            .get().addOnSuccessListener {
                for (result in it.documents) {
                    user = result.toObject<User>()!!
                    commentUsers.add(user)
                }
                _commentUsers.value = commentUsers
                Timber.tag("neil").i("success load documents = %s", commentUsers)

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