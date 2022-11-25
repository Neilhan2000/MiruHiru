package com.neil.miruhiru.verify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Challenge

class VerifyViewModel : ViewModel() {

    private val _unverifiedChallenges = MutableLiveData<List<Challenge>>()
    val unverifiedChallenges: LiveData<List<Challenge>>
        get() = _unverifiedChallenges

    fun loadUnverifiedChallenges() {
        val db = Firebase.firestore
        val challengeList = mutableListOf<Challenge>()

        db.collection("unverifiedCustoms").orderBy("createdTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                it.documents.forEach {
                    val challenge = it.toObject<Challenge>()
                    if (challenge != null) {
                        challengeList.add(challenge)
                    }
                }
                _unverifiedChallenges.value = challengeList
            }
    }
}