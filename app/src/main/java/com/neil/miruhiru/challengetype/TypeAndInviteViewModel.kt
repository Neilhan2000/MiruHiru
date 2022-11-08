package com.neil.miruhiru.challengetype

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import timber.log.Timber
import java.lang.Exception

class TypeAndInviteViewModel() : ViewModel() {

    private val _navigateToTaskFragment = MutableLiveData<Boolean?>()
    val navigateToTaskFragment: LiveData<Boolean?>
        get() = _navigateToTaskFragment

    private var eventDocumentId = ""

    fun postEvent(eventId: String, challenge: Challenge) {
        val event = hashMapOf(
            "id" to eventId,
            "members" to listOf<String>(),
            "startTime" to Timestamp.now(),
            "isCompleted" to false,
            "challengeId" to challenge.id,
            "progress" to listOf<Int>(),
            "stage" to challenge.stage
        )

        val db = Firebase.firestore
        db.collection("events")
            .add(event)
            .addOnSuccessListener { documentReference ->
                Timber.i("DocumentSnapshot added with ID: %s", documentReference.id)
                _navigateToTaskFragment.value = true
                eventDocumentId = documentReference.id
                addMainUserToEvent()
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error adding document")
            }
    }

    private fun addMainUserToEvent() {
        // add members
        val db = Firebase.firestore
        db.collection("events").document(eventDocumentId)
            .update("members", FieldValue.arrayUnion(UserManager.userId))
            .addOnSuccessListener { documentReference ->
                Timber.i("DocumentSnapshot added with ID: %s", documentReference)
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error adding document")
            }
        // initialize progress
        db.collection("events").document(eventDocumentId)
            .update("progress", FieldValue.arrayUnion(1))
            .addOnSuccessListener { documentReference ->
                Timber.i("DocumentSnapshot added with ID: %s", documentReference)
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error adding document")
            }
    }

    fun updateUserCurrentEvent(eventId: String) {
        val db = Firebase.firestore
        var userDocumentId = ""
        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener { result ->
                userDocumentId = result.documents[0].id

                db.collection("users").document(userDocumentId)
                    .update("currentEvent", eventId)
                // update local user data
                UserManager.getUser()
            }

    }

    fun addOtherUserToEvent(eventId: String) {
        val db = Firebase.firestore
        var eventDocumentId = ""
        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener { result ->
                eventDocumentId = result.documents[0].id

                // add members
                db.collection("events").document(eventDocumentId)
                    .update("members", FieldValue.arrayUnion(UserManager.userId))
                    .addOnSuccessListener { documentReference ->
                        Timber.i("DocumentSnapshot added with ID: %s", documentReference)
                    }
                    .addOnFailureListener { e ->
                        Timber.i(e, "Error adding document")
                    }
                // update progress, here we read progress array and reset it on firebase
                db.collection("events").document(eventDocumentId)
                    .get()
                    .addOnSuccessListener { documentReference ->
                        // read
                        val progress = documentReference.data?.get("progress") as MutableList<Int>
                        // add
                        progress.add(1)
                        // reset
                        db.collection("events").document(eventDocumentId)
                            .update("progress", progress)
                    }
                    .addOnFailureListener { e ->
                        Timber.i(e, "Error adding document")
                    }
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error getting document")
            }
    }

    fun navigateToTaskFragmentCompleted() {
        _navigateToTaskFragment.value = null
    }


}