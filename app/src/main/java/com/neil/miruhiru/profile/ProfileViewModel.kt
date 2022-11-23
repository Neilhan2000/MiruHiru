package com.neil.miruhiru.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Task
import timber.log.Timber

class ProfileViewModel : ViewModel() {

    private val _completedChallengeList = MutableLiveData<List<Challenge>>()
    val completedList: LiveData<List<Challenge>>
        get() = _completedChallengeList

    private val completedChallengeList = mutableListOf<Challenge>()
    var position = 0

    val eventList = mutableListOf<Event>()

    init {
        // load CompletedList here to prevent fragment onCreateView reloading
        loadCompletedChallenge()
    }

    private fun loadCompletedChallenge() {
        Timber.i("position = $position")
        val db = Firebase.firestore
        val eventId = UserManager.user.completedEvents[position]

        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener {

                val event = it.documents[0].toObject<Event>()
                if (event?.personal == false) {
                    db.collection("challenges").whereEqualTo("id", event.challengeId)
                        .get()
                        .addOnSuccessListener {
                            Timber.i("event document id = ${it.documents[0].id}")
                            Timber.i("challenge id = ${event.challengeId}")

                            val challenge = it.documents[0].toObject<Challenge>()
                            if (challenge != null) {
                                completedChallengeList.add(challenge)
                            }
                            if (completedChallengeList.size == UserManager.user.completedEvents.size) {
                                _completedChallengeList.value = completedChallengeList.reversed()
                                position = 0
                            } else {
                                position ++
                                loadCompletedChallenge()
                            }
                        }
                } else {
                    db.collection("users").whereEqualTo("id", event?.members?.first())
                        .get()
                        .addOnSuccessListener {

                            it.documents[0].reference.collection("customChallenges")
                                .whereEqualTo("id", event?.challengeId)
                                .get()
                                .addOnSuccessListener {

                                    val challenge = it.documents[0].toObject<Challenge>()
                                    if (challenge != null) {
                                        completedChallengeList.add(challenge)
                                    }
                                    if (completedChallengeList.size == UserManager.user.completedEvents.size) {
                                        _completedChallengeList.value = completedChallengeList.reversed()
                                        position = 0
                                    } else {
                                        position ++
                                        loadCompletedChallenge()
                                    }
                                }

                        }

                }
            }

    }
}