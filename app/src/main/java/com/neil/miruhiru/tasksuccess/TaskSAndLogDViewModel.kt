package com.neil.miruhiru.tasksuccess

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Event
import timber.log.Timber
import java.lang.Exception

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

    var currentStage = -1
    var documentId = ""
    var stageNumber = -1

    private fun loadEvent(eventId: String) {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id" ,eventId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    documentId = document.id
                    val event = document.toObject<Event>()
                    _event.value = event

                    if (event.progress.first() == event.progress.last()) {
                        currentStage = event.progress.maxOrNull() ?: -1
                    } else {
                        currentStage = event.progress.maxOrNull() ?: -1
                    }


                    Timber.i("current event progress ${event.progress}")
                    Timber.i("current event stage $currentStage")


                    stageNumber = event.stage
                    _getCurrentStage.value = currentStage

                    detectUsersProgress()
                }
            }
            .addOnFailureListener { exception ->
                Timber.i("Error getting documents.", exception)
            }

    }

    fun updateProgress() {
        val db = Firebase.firestore

        // update progress, here we read progress array and reset it on firebase
        db.collection("events").document(documentId)
            .get()
            .addOnSuccessListener { documentReference ->
                // read
                val progress = documentReference.data?.get("progress") as MutableList<Int>

                Timber.i("progress $progress")
                if (progress.first() != progress.last()) {
                    // remove first element
                    progress.removeAt(0)
                    // add
                    progress.add(currentStage)
                } else {
                    // remove first element
                    progress.removeAt(0)
                    // add
                    progress.add(currentStage + 1)
                }
                // reset
                db.collection("events").document(documentId)
                    .update("progress", progress)
                _navigateToTaskFragment.value = true
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error adding document")
            }
    }

    fun navigateToTaskFragmentCompleted() {
        _navigateToTaskFragment.value = false
    }


    fun completeEvent() {

        // set event isCompleted to true
        val db = Firebase.firestore
        val isCompleted = hashMapOf(
            "isCompleted" to true
        )
        db.collection("events").document(documentId)
            .set(isCompleted, SetOptions.merge())
//        UserManager.clearChallengeId()

        // clean user current event data
        var userDocumentId = ""
        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener { result ->
                userDocumentId = result.documents[0].id

                db.collection("users").document(userDocumentId)
                    .update("currentEvent", null)
                // update local user data
                UserManager.getUser()
            }
    }

    fun completeChallenge() {
        val db = Firebase.firestore
        // add user to challenge completed list
        UserManager.userChallengeDocumentId?.let { db.collection("challenges").document(it)
            .update("completedList", listOf(UserManager.userId))}
    }

    private val _isButtonClickable = MutableLiveData<Boolean>()
    val isButtonClickable: LiveData<Boolean>
        get() = _isButtonClickable

    private fun detectUsersProgress() {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener { result ->
                val eventDocumentId = result.documents[0].id

                db.collection("events").document(eventDocumentId)
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
}