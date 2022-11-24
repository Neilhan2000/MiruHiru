package com.neil.miruhiru.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Challenge

class CommunityViewModel : ViewModel() {

    private val _challengeList = MutableLiveData<List<Challenge>>()
    val challengeList: LiveData<List<Challenge>>
        get() = _challengeList

    private val _bannerList = MutableLiveData<List<Challenge>>()
    val bannerList: LiveData<List<Challenge>>
        get() = _bannerList

    fun loadChallengesByPopularity() {
        val db = Firebase.firestore

        db.collection("challenges").orderBy("commentQuantity", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                val challengeList = mutableListOf<Challenge>()

                it.documents.forEach {
                    val challenge = it.toObject<Challenge>()
                    if (challenge != null) {
                        challengeList.add(challenge)
                    }
                }
                _challengeList.value = challengeList
                if (challengeList.size > 5) {
                    _bannerList.value = challengeList.take(5)
                } else {
                    _bannerList.value = challengeList
                }
            }
    }

    fun sortByTag(type: String) {

    }
}