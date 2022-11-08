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
import com.neil.miruhiru.data.Event
import timber.log.Timber
import java.lang.Exception

class LogDialogViewModel(): ViewModel() {

    init {
        val challengeId = "2WBySSd68w3VrA08eLGj"
        loadEvent(challengeId)
    }

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    var currentStage = -1
    var documentId = ""
    var stageNumber = -1

    private fun loadEvent(challengeId: String) {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id" ,"0")
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
            Timber.i("documentid $documentId")
        } catch (e: Exception) {
            Timber.i("Error ${e.message}")
        }
    }

    fun completeChallenge() {

    }
}