package com.neil.miruhiru.join

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.User
import timber.log.Timber

class JoinEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _navigateToTaskFragment = MutableLiveData<String?>()
    val navigateToTaskFragment: LiveData<String?>
        get() = _navigateToTaskFragment




    // update local user data (especially current event)
    private fun getUser(type: String) {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id" , UserManager.userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject<User>()
                    UserManager.user = user
                    Timber.i("get user $user")
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

                if (result.size() != 0) {
                    eventDocumentId = result.documents[0].id

                    // add event members
                    val userId = UserManager.userId // Usermanager.userid
                    db.collection("events").document(eventDocumentId)
                        .update("members", FieldValue.arrayUnion(userId))
                        .addOnSuccessListener { documentReference ->

                            // update user current event
                            db.collection("users").whereEqualTo("id", UserManager.userId)
                                .get()
                                .addOnSuccessListener {
                                    val userDocumentId = it.documents[0].id

                                    db.collection("users").document(userDocumentId)
                                        .update("currentEvent", eventId)
                                        .addOnSuccessListener {
                                            loadChallengeIdByEventId(eventId, type)
                                        }
                                }
                        }
                        .addOnFailureListener { e ->
                            Timber.i(e, "Error adding document")
                        }
                    // add event currentMembers
                    db.collection("events").document(eventDocumentId)
                        .update("currentMembers", FieldValue.arrayUnion(userId))

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
                } else {
                    Toast.makeText(getApplication(), "ID輸入錯誤", Toast.LENGTH_SHORT).show()
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