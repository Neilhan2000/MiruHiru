package com.neil.miruhiru

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.User
import timber.log.Timber

class MainViewModel: ViewModel() {

    fun cleanEventSingle() {
        val db = Firebase.firestore
        var userDocumented = ""

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                userDocumented = it.documents[0].id

                db.collection("users").document(userDocumented)
                    .update("currentEvent", "")
                    .addOnSuccessListener {
                        UserManager.currentStage = null
                        UserManager.getUser()
                    }
            }
    }

    fun cleanEventMultiple() {
        val db = Firebase.firestore
        var userDocumented = ""
        var eventDocumentId = ""

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                val user = it.documents[0].toObject<User>()
                userDocumented = it.documents[0].id

                db.collection("events").whereEqualTo("id", user?.currentEvent)
                    .get()
                    .addOnSuccessListener {
                        eventDocumentId = it.documents[0].id
                        val event = it.documents[0].toObject<Event>()
                        val progress = event?.progress as MutableList<Int>
                        progress.remove(UserManager.currentStage)

                        // remove progress
                        db.collection("events").document(eventDocumentId)
                            .update("progress", progress)
                            .addOnSuccessListener { UserManager.currentStage = null }

                        // remove user current event
                        db.collection("users").document(userDocumented)
                            .update("currentEvent", "")
                            .addOnFailureListener {
                                UserManager.getUser()
                            }

                    }

            }

    }
}