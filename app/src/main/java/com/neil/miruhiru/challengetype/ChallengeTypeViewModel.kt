package com.neil.miruhiru.challengetype

import android.util.Log
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
import com.neil.miruhiru.data.User
import timber.log.Timber
import java.lang.Exception

class ChallengeTypeViewModel() : ViewModel() {

    private val _navigateToTaskFragment = MutableLiveData<String?>()
    val navigateToTaskFragment: LiveData<String?>
        get() = _navigateToTaskFragment

    private var eventDocumentId = ""

    fun postEvent(eventId: String, challenge: Challenge, type: String) {
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
                eventDocumentId = documentReference.id
                addMainUserToEvent()
                updateUserCurrentEvent(eventId, type)
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error adding document")
            }
    }


    private fun addMainUserToEvent() {
        // add event members
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

    fun updateUserCurrentEvent(eventId: String, type: String) {
        val db = Firebase.firestore
        var userDocumentId = ""
        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener { result ->
                userDocumentId = result.documents[0].id

                db.collection("users").document(userDocumentId)
                    .update("currentEvent", eventId)
                loadChallengeIdByEventId(eventId, type)
            }

    }

    // update local user data (especially current event)
    private fun getUser(type: String) {

        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id" , UserManager.userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject<User>()
                    UserManager.user = user
                    _navigateToTaskFragment.value = type
                }
            }
            .addOnFailureListener { exception ->
                Log.i("neil", "Error getting documents.", exception)
            }
    }

    fun addScanUserToEvent(eventId: String, type: String) {
        val db = Firebase.firestore
        var eventDocumentId = ""

        // get event document id
        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener { result ->
                Timber.i("result ${result.documents}")
                eventDocumentId = result.documents[0].id

                // add event members
                db.collection("events").document(eventDocumentId)
                    .update("members", FieldValue.arrayUnion(UserManager.userId))
                    .addOnSuccessListener { documentReference ->
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
                            .addOnSuccessListener {
                                loadChallengeIdByEventId(eventId, type)
                            }
                    }
                    .addOnFailureListener { e ->
                        Timber.i(e, "Error adding document")
                    }
            }
            .addOnFailureListener { e ->
                Timber.i(e, "Error getting document")
            }
    }

    private fun loadChallengeIdByEventId(eventId: String, type: String) {
        val db = Firebase.firestore
        var eventDocumentId = ""
        var challengeId = ""
        var challengeDocumentId = ""

        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener { result ->
                eventDocumentId = result.documents[0].id

                db.collection("events").document(eventDocumentId)
                    .get()
                    .addOnSuccessListener { result->
                        val event = result.toObject<Event>()
                        challengeId = event?.challengeId ?: ""

                        db.collection("challenges").whereEqualTo("id", challengeId)
                            .get().addOnSuccessListener { result ->
                                challengeDocumentId = result.documents[0].id
                                UserManager.userChallengeDocumentId = challengeDocumentId
                                getUser(type)
                            }
                    }
            }
    }

    fun navigateToTaskFragmentCompleted() {
        _navigateToTaskFragment.value = null
    }


}