package com.neil.miruhiru.community

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.network.LoadingStatus

class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelApplication = application

    private val _communityChallengeList = MutableLiveData<List<Challenge>>()
    val communityChallengeList: LiveData<List<Challenge>>
        get() = _communityChallengeList

    private val _bannerList = MutableLiveData<List<Challenge>>()
    val bannerList: LiveData<List<Challenge>>
        get() = _bannerList

    private lateinit var challengeList: MutableList<Challenge>
    private lateinit var sortedList: MutableList<Challenge>

    private val _loadingStatus = MutableLiveData<LoadingStatus>()
    val loadingStatus: LiveData<LoadingStatus>
        get() = _loadingStatus

    private fun startLoading() {
        _loadingStatus.value = LoadingStatus.LOADING
    }

    private fun loadingCompleted() {
        _loadingStatus.value = LoadingStatus.DONE
    }

    private fun loadingError() {
        _loadingStatus.value = LoadingStatus.ERROR
    }

    fun loadChallengesByPopularity() {
        startLoading()

        val db = Firebase.firestore

        db.collection("challenges").orderBy("commentQuantity", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                challengeList = mutableListOf<Challenge>()

                it.documents.forEach {
                    val challenge = it.toObject<Challenge>()
                    if (challenge != null) {
                        challengeList.add(challenge)
                    }
                }
                _communityChallengeList.value = challengeList
                sortedList = challengeList
                if (challengeList.size > 5) {
                    _bannerList.value = challengeList.take(5)
                } else {
                    _bannerList.value = challengeList
                }
                loadingCompleted()
            }
    }

    fun sortChallenges(type: String, searchText: String) {
        sortedList = mutableListOf()
        if (type == viewModelApplication.getString(R.string.popularity)) {
            challengeList.forEach { challenge ->
                if (challenge.name.lowercase().contains(searchText.lowercase())) {
                    sortedList.add(challenge)
                }
            }
            _communityChallengeList.value = sortedList
        } else {
            challengeList.forEach { challenge ->
                if (challenge.type == type) {
                    sortedList.add(challenge)
                }
            }

            val list = mutableListOf<Challenge>()
            sortedList.forEach { challenge ->
                if (challenge.name.lowercase().contains(searchText.lowercase())) {
                    list.add(challenge)
                }
            }
            sortedList = list
            _communityChallengeList.value = sortedList
        }
    }
}