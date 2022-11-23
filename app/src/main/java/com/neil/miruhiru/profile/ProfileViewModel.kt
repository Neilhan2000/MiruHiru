package com.neil.miruhiru.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Task

class ProfileViewModel : ViewModel() {

    private val _completedChallengeList = MutableLiveData<List<Challenge>>()
    val completedChallengeList: LiveData<List<Challenge>>
        get() = _completedChallengeList

    fun loadFinishedLogs() {
        val db = Firebase.firestore
        val completedChallengeList = mutableListOf<Challenge>()

        UserManager.user.completedEvents.forEach { eventId ->
            db.collection("events").whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener {

                   val event = it.documents[0].toObject<Event>()

                    db.collection("challenge").whereEqualTo("id", event?.challengeId)
                        .get()
                        .addOnSuccessListener {

                            val challenge = it.documents[0].toObject<Challenge>()
                            if (challenge != null) {
                                completedChallengeList.add(challenge)
                            }
                        }
                }
            if (completedChallengeList.size == UserManager.user.completedEvents.size) {
                _completedChallengeList.value = completedChallengeList
            }
        }
    }
}