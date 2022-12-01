package com.neil.miruhiru.likeChallenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber

class LikeChallengeViewModel : ViewModel() {

    private val _likeChallengeList = MutableLiveData<List<Challenge>>()
    val likeChallengeList: LiveData<List<Challenge>>
        get() = _likeChallengeList

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

    fun loadLikeChallenges() {
        startLoading()

        val db = Firebase.firestore
        var likeChallengeList = mutableListOf<Challenge>()

        UserManager.user.likeChallenges.forEach { challengeId ->
            db.collection("challenges").whereEqualTo("id", challengeId)
                .get()
                .addOnSuccessListener {
                    val challenge = it.documents[0].toObject<Challenge>()

                    if (challenge != null) {
                        likeChallengeList.add(challenge)
                    }

                    if (likeChallengeList.size == UserManager.user.likeChallenges.size) {
                        // sort challenge list by add to like time
                        val desiredOrder = UserManager.user.likeChallenges
                        val challengeById = likeChallengeList.associateBy { it.id }
                        val sortedChallengeList = desiredOrder.map { challengeById[it] }
                        likeChallengeList = mutableListOf<Challenge>()
                        sortedChallengeList.forEach {
                            if (it != null) {
                                likeChallengeList.add(it)
                            }
                        }

                        _likeChallengeList.value = likeChallengeList.reversed()
                        loadingCompleted()
                    }
                }

        }
        if (UserManager.user.likeChallenges.isEmpty()) {
            _likeChallengeList.value = mutableListOf()
            loadingCompleted()
        }
    }
}