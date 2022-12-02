package com.neil.miruhiru.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.MiruHiruApplication
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

//    init {
//        // load CompletedList here to prevent fragment onCreateView reloading
//        loadCompletedChallenge()
//    }

    private val viewModelApplication = application

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(
        viewModelApplication, googleSignInOptions)

    private val _completedChallengeList = MutableLiveData<List<Challenge>>()
    val completedList: LiveData<List<Challenge>>
        get() = _completedChallengeList

    private var completedChallengeList = mutableListOf<Challenge>()

    val eventList = mutableListOf<Event>()

//    lateinit var _loadingStatus: MutableLiveData<LoadingStatus>
//    val loadingStatus: LiveData<LoadingStatus>
//        get() = _loadingStatus

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

    // prevent adding repeated challenge to list
    fun cleanCompletedChallengeList() {
        completedChallengeList = mutableListOf()
    }

    fun loadCompletedChallenge() {
        startLoading()
        if (UserManager.user.completedEvents.isEmpty()) {
            loadingCompleted()
        }
        Timber.i("completeEvents" + UserManager.user.completedEvents)

        val db = Firebase.firestore

        UserManager.user.completedEvents.forEach { eventId ->
            db.collection("events").whereEqualTo("id", eventId)
                .get()
                .addOnSuccessListener {

                    val event = it.documents[0].toObject<Event>()
                    if (event != null) {
                        eventList.add(event)
                    }

                    if (event?.personal == false) {
                        db.collection("challenges").whereEqualTo("id", event.challengeId)
                            .get()
                            .addOnSuccessListener {

                                val challenge = it.documents[0].toObject<Challenge>()
                                if (challenge != null) {
                                    completedChallengeList.add(challenge)
                                }
                                Timber.i("user manager list" + UserManager.user.completedEvents.size +
                                        "completedChallengeList" + completedChallengeList.size)
                                if (completedChallengeList.size == UserManager.user.completedEvents.size) {

                                    // sort event to list
                                    val eventDesiredOrder = UserManager.user.completedEvents
                                    val eventById = eventList.associateBy { it.id }
                                    val sortedEventList = eventDesiredOrder.map { eventById[it] }

                                    // use sorted event list to make a sorted challenge id list
                                    val challengeOrder = mutableListOf<String>()
                                    sortedEventList.forEach {
                                        it?.challengeId?.let { it1 -> challengeOrder.add(it1) }
                                    }

                                    // sort challenge list by completed time
                                    val challengeById = completedChallengeList.associateBy { it.id }
                                    val sortedChallengeList = challengeOrder.map { challengeById[it] }
                                    completedChallengeList = mutableListOf<Challenge>()
                                    sortedChallengeList.forEach {
                                        if (it != null) {
                                            completedChallengeList.add(it)
                                        }
                                    }
                                    _completedChallengeList.value = completedChallengeList.reversed()
                                    loadingCompleted()

                                }
                            }
                    } else {
                        Timber.i("personal")
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

                                            // sort event to list
                                            val eventDesiredOrder = UserManager.user.completedEvents
                                            val eventById = eventList.associateBy { it.id }
                                            val sortedEventList = eventDesiredOrder.map { eventById[it] }

                                            // sort event to list
                                            val challengeOrder = mutableListOf<String>()
                                            sortedEventList.forEach {
                                                it?.challengeId?.let { it1 -> challengeOrder.add(it1) }
                                            }

                                            // sort challenge list by completed time
                                            val challengeById = completedChallengeList.associateBy { it.id }
                                            val sortedChallengeList = challengeOrder.map { challengeById[it] }
                                            completedChallengeList = mutableListOf<Challenge>()
                                            sortedChallengeList.forEach {
                                                if (it != null) {
                                                    completedChallengeList.add(it)
                                                }
                                            }

                                            _completedChallengeList.value = completedChallengeList.reversed()
                                            loadingCompleted()
                                        }
                                    }

                            }

                    }
                }
        }

    }

    // sign out
    private val _navigateToSignInFragment = MutableLiveData<Boolean>()
    val navigateToSignInFragment: LiveData<Boolean>
        get() = _navigateToSignInFragment

    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            _navigateToSignInFragment.value = true
            UserManager.readNotifications = null
        }
    }

    fun navigateToSignInFragmentCompleted() {
        _navigateToSignInFragment.value = false
    }
}