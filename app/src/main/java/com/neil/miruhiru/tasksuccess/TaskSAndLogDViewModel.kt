package com.neil.miruhiru.tasksuccess

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    var currentStage = -1
    var documentId = ""
    var stageNumber = -1

    private fun loadEvent(eventId: String) {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id" ,eventId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val event = document.toObject<Event>()
                    _event.value = event
                    currentStage = event.progress.minOrNull() ?: -1
                    stageNumber = event.stage
                    documentId = document.id
                    Timber.i("document.id ${document.id}")
                    Timber.i("documentid $documentId")
                }
            }
            .addOnFailureListener { exception ->
                Timber.i("Error getting documents.", exception)
            }

    }

    fun updateProgress() {
        try {
            val db = Firebase.firestore
            val progress = hashMapOf(
                "progress" to listOf<Int>(currentStage + 1)
            )
            db.collection("events").document(documentId)
                .set(progress, SetOptions.merge())
            Timber.i("documentid $documentId")
        } catch (e: Exception) {
            Timber.i("Error ${e.message}")
        }
    }

    fun completeEvent() {
        try {
            val db = Firebase.firestore
            val isCompleted = hashMapOf(
                "isCompleted" to true
            )
            db.collection("events").document(documentId)
                .set(isCompleted, SetOptions.merge())
            UserManager.clearChallengeId()
        } catch (e: Exception) {
            Timber.i("Error ${e.message}")
        }
    }

    fun completeChallenge() {
        val db = Firebase.firestore
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
}