package com.neil.miruhiru.tasksuccess

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.User
import timber.log.Timber

class TaskSAndLogDViewModel(): ViewModel() {

    init {
        loadEvent(UserManager.user.currentEvent)
    }

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _navigateToTaskFragment = MutableLiveData<Boolean>()
    val navigateToTaskFragment: LiveData<Boolean>
        get() = _navigateToTaskFragment

    private val _getCurrentStage = MutableLiveData<Int>()
    val getCurrentStage: LiveData<Int>
        get() = _getCurrentStage

    private val _navigateToLogFragment = MutableLiveData<Boolean>()
    val navigateToLogFragment: LiveData<Boolean>
        get() = _navigateToLogFragment

    private val _isKicked = MutableLiveData<Boolean>()
    val isKicked: LiveData<Boolean>
        get() = _isKicked

    var currentStage = -1
    var stageNumber = -1
    var kickNumber = 1

    private fun loadEvent(eventId: String) {

        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id" ,eventId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val event = document.toObject<Event>()
                    _event.value = event

                    if (event.progress.first() == event.progress.last()) {
                        currentStage = (event.progress.minOrNull() ?: -1) - 1
                    } else {
                        currentStage = event.progress.minOrNull() ?: -1
                    }


//                    Timber.i("current event progress ${event.progress}")
//                    Timber.i("current event stage $currentStage")


                    stageNumber = event.stage
                    _getCurrentStage.value = currentStage
                    // set user manager current stage
                    UserManager.currentStage = currentStage + 1
                    Timber.i("set current stage ${UserManager.currentStage}")

                    detectUsersProgress()
                }
            }
            .addOnFailureListener { exception ->
                Timber.i("Error getting documents.", exception)
            }

    }

    fun updateProgress() {
        val db = Firebase.firestore
        var eventDocumentId = ""

        // get document id
        db.collection("events").whereEqualTo("id" ,UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    eventDocumentId = document.id
                }
                // update progress, here we read progress array and reset it on firebase
                db.collection("events").document(eventDocumentId)
                    .get()
                    .addOnSuccessListener { documentReference ->
                        // read
                        val progress = documentReference.data?.get("progress") as MutableList<Int>

                        Timber.i("progress $progress")

                        if (progress.first() == progress.last()) {
                            // remove first element
                            progress.removeAt(0)
                            // add
                            progress.add(currentStage + 2)
                            UserManager.currentStage = currentStage + 2
                        } else {
                            // remove first element
                            progress.removeAt(0)
                            // add
                            progress.add(currentStage + 1)
                            UserManager.currentStage = currentStage + 1
                        }
                        Timber.i("set current stage ${UserManager.currentStage}")
                        // reset
                        db.collection("events").document(eventDocumentId)
                            .update("progress", progress)
                        _navigateToTaskFragment.value = true
                    }
                    .addOnFailureListener { e ->
                        Timber.i(e, "Error adding document")
                    }
            }
    }

    fun navigateToTaskFragmentCompleted() {
        _navigateToTaskFragment.value = false
    }

    fun navigateToLogFragmentCompleted() {
        _navigateToLogFragment.value = false
    }

    fun detectUserKicked() {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id" , UserManager.user.currentEvent)
            .addSnapshotListener { value, error ->
                value?.documents?.get(0)?.let {
                    val event = it.toObject<Event>()

                    if (event?.currentMembers?.contains(UserManager.userId) == false) {
                        if (kickNumber == 1) {
                            db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
                                .get()
                                .addOnSuccessListener {
                                    val eventDocumentId = it.documents[0].id
                                    val event = it.documents[0].toObject<Event>()
                                    val progress: MutableList<Int> = event?.progress as MutableList<Int>
                                    progress.remove(UserManager.currentStage)
                                    Timber.i("kick number $kickNumber progress $progress")

                                    kickNumber ++
                                    _isKicked.value = true
                                    // remove progress
                                    db.collection("events").document(eventDocumentId)
                                        .update("progress", progress)
                                        .addOnSuccessListener {

                                            // remove user current Event
                                            db.collection("users").whereEqualTo("id", UserManager.userId)
                                                .get()
                                                .addOnSuccessListener {
                                                    val userDocumentId = it.documents[0].id

                                                    db.collection("users").document(userDocumentId)
                                                        .update("currentEvent", "")
                                                        .addOnSuccessListener {
                                                            UserManager.getUser()
                                                        }
                                                }
                                        }

                                }
                        }
                    }
                }

            }
    }


    fun completeEvent() {

        // set event isCompleted to true
        val db = Firebase.firestore
        val isCompleted = hashMapOf(
            "isCompleted" to true
        )
        val endTime = hashMapOf(
            "endTime" to Timestamp.now()
        )
        var eventDocumentId = ""

        db.collection("events").whereEqualTo("id" ,UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    eventDocumentId = document.id
                }

                // set event completed
                db.collection("events").document(eventDocumentId)
                    .set(isCompleted, SetOptions.merge())

                // update user endTime
                db.collection("events").document(eventDocumentId)
                    .set(endTime, SetOptions.merge())
                    .addOnSuccessListener {

                        // clean user current event data
                        var userDocumentId = ""

                        db.collection("users").whereEqualTo("id", UserManager.userId)
                            .get()
                            .addOnSuccessListener { result ->
                                userDocumentId = result.documents[0].id

                                Timber.i("complete event userid = ${UserManager.userId}")
                                db.collection("users").document(userDocumentId)
                                    .update("currentEvent", "")
                                    .addOnSuccessListener {

                                        // add event to user completedEvents list
                                        db.collection("users").document(userDocumentId)
                                            .update("completedEvents", FieldValue.arrayUnion(UserManager.user.currentEvent))
                                            .addOnSuccessListener {
                                                // clean local user current event data
                                                getUser()
                                            }
                                    }

                            }

                    }
            }
    }

    private fun getUser() {
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("id" , UserManager.userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject<User>()
                    UserManager.user = user
                    UserManager.currentStage = null
                    _navigateToLogFragment.value = true
                }
            }

    }

    fun completeChallenge() {
        val db = Firebase.firestore
        // add user to challenge completed list
        UserManager.userChallengeDocumentId?.let { db.collection("challenges").document(it)
            .update("completedList", FieldValue.arrayUnion(UserManager.userId))}
    }

    private val _isButtonClickable = MutableLiveData<Boolean>()
    val isButtonClickable: LiveData<Boolean>
        get() = _isButtonClickable

    private lateinit var listenerRegistration: ListenerRegistration
    private fun detectUsersProgress() {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener { result ->
                val eventDocumentId = result.documents[0].id

                listenerRegistration = db.collection("events").document(eventDocumentId)
                    .addSnapshotListener { value, error ->
                        val event = value?.toObject<Event>()
                        event?.progress?.let {
                            // check if all user have same progress
                            Timber.i("progress changes = $it")
                            _isButtonClickable.value = it.first() == it.last()
                        }
                    }
            }
    }

    fun removeDetectUsersProgress() {
        listenerRegistration.remove()
    }
}