package com.neil.miruhiru.challengedetail

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mapbox.geojson.Point
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.*
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat

class ChallengeDetailViewModel() : ViewModel() {

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

    private val _hasCurrentEvent = MutableLiveData<Boolean?>()
    val hasCurrentEvent: LiveData<Boolean?>
        get() = _hasCurrentEvent

    private val _authorName = MutableLiveData<String>()
    val authorName: LiveData<String>
        get() = _authorName

    private val _loadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus>
        get() = _loadingStatus

    private lateinit var comments: MutableList<Comment>

    private fun startLoading() {
        _loadingStatus.value = LoadingStatus.LOADING
    }

    private fun loadingCompleted() {
        _loadingStatus.value = LoadingStatus.DONE
    }

    private fun loadingError() {
        _loadingStatus.value = LoadingStatus.ERROR
    }

    fun checkHasCurrentEvent(challengeId: String) {
        startLoading()
        val db = Firebase.firestore

        if (UserManager.user.currentEvent.isNotEmpty()) {
            db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
                .get()
                .addOnSuccessListener {
                    val event = it.documents[0].toObject<Event>()
                    _hasCurrentEvent.value = event?.challengeId == challengeId
                    loadingCompleted()
                }
        } else {
            _hasCurrentEvent.value = false
            loadingCompleted()
        }
    }

    fun navigateToChallengeTypeFragmentCompleted() {
        _hasCurrentEvent.value = null
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

    fun loadChallenge(challengeId: String) {
        startLoading()
        val db = Firebase.firestore

        db.collection("challenges").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener { result ->
                val challenge = result.documents[0].toObject<Challenge>()
                challenge?.let { _challenge.value = it }
                challengeDocumentId = result.documents[0].id

                loadTasks()
                challenge?.author?.let { loadAuthor(it) }
            }

    }

    fun loadPersonalChallenge(customChallengeId: String) {
        startLoading()
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id" , UserManager.userId)
            .get()
            .addOnSuccessListener {
                val userDocumentId = it.documents[0].id
                UserManager.userChallengeDocumentId = userDocumentId

                db.collection("users").document(userDocumentId).collection("customChallenges")
                    .whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {
                        val customDocumentId = it.documents[0].id
                        val customChallenge = it.documents[0].toObject<Challenge>()
                        _challenge.value = customChallenge!!

                        loadPersonalTasks(userDocumentId, customDocumentId)
                    }
            }
    }

    private fun loadAuthor(authorId: String) {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", authorId)
            .get()
            .addOnSuccessListener {
                val author = it.documents[0].toObject<User>()
                _authorName.value = author?.name
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
                loadingCompleted()
            }
            .addOnFailureListener { exception ->
                Timber.i(exception, "Error getting documents.")
            }

    }

    private fun loadPersonalTasks(userDocumentId: String, customDocumentId: String) {
        val db = Firebase.firestore

        db.collection("users").document(userDocumentId).collection("customChallenges")
            .document(customDocumentId).collection("tasks")
            .get()
            .addOnSuccessListener {
                val taskList = mutableListOf<Task>()
                Timber.i("documents ${it.documents}")

                it.documents.forEach { document ->
                    val task = document.toObject<Task>()
                    if (task != null) {
                        taskList.add(task)
                    }
                }
                _taskList.value = taskList
                loadingCompleted()
            }
    }

    fun loadComments() {
        val db = Firebase.firestore
        comments = mutableListOf<Comment>()

        db.collection("challenges").document(challengeDocumentId)
            .collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val commentUsers = mutableListOf<User>()

                for (document in result) {
                    val comment = document.toObject<Comment>()
                    if (!UserManager.user.blockList.contains(comment.userId)) {
                        comments.add(comment)

                        // add user to comment user list
                        db.collection("users").whereEqualTo("id", comment.userId)
                            .get().addOnSuccessListener {
                                val user = it.documents[0].toObject<User>()
                                if (user != null) {
                                    commentUsers.add(user)
                                    _commentList.value = comments
                                    _commentUsers.value = commentUsers
                                }
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

    fun roundOffDecimal(number: Float): Float? {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toFloat()
    }

    fun likeChallenge(challengeId: String) {
        val db = Firebase.firestore

        // challenge
        db.collection("challenges").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener {
                it.documents[0].reference.update("likeList", FieldValue.arrayUnion(UserManager.userId))
            }

        // user
        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                it.documents[0].reference.update("likeChallenges", FieldValue.arrayUnion(challengeId))
                    .addOnSuccessListener {
                        UserManager.getUser()
                    }
            }
    }

    fun unLikeChallenge(challengeId: String) {
        val db = Firebase.firestore

        // challenge
        db.collection("challenges").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener {
                it.documents[0].reference.update("likeList", FieldValue.arrayRemove(UserManager.userId))
            }

        // user
        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                it.documents[0].reference.update("likeChallenges", FieldValue.arrayRemove(challengeId))
                    .addOnSuccessListener {
                        UserManager.getUser()
                    }
            }
    }

    fun blockUser(position: Int) {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference
                    .update("blockList", FieldValue.arrayUnion(commentList.value?.get(position)?.userId))
                    .addOnSuccessListener {
                        comments.removeIf {
                            it.userId == comments[position].userId
                        }
                        _commentList.value = comments
                        _commentUsers.value = commentUsers.value
                        UserManager.getUser()
                    }
            }
    }

    fun isLike(challenge: Challenge): Boolean {
        challenge.likeList.forEach { likeUser ->
            if (likeUser == UserManager.userId) {
                return true
            }
        }
        return false
    }
}