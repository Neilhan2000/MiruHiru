package com.neil.miruhiru.verifydetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Task
import timber.log.Timber
import kotlin.reflect.full.memberProperties

class VerifyDetailViewModel : ViewModel() {

    private val _unverifiedChallenge = MutableLiveData<Challenge>()
    val unverifiedChallenge: LiveData<Challenge>
        get() = _unverifiedChallenge

    private val _unverifiedTasks = MutableLiveData<List<Task>>()
    val unverifiedTasks: LiveData<List<Task>>
        get() = _unverifiedTasks

    private val _navigateUp = MutableLiveData<Boolean>()
    val navigateUp: LiveData<Boolean>
        get() = _navigateUp

    fun loadUnverifiedChallenge(challengeId: String) {
        val db = Firebase.firestore

        db.collection("unverifiedCustoms").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener {
                val challenge = it.documents[0].toObject<Challenge>()
                challenge?.let { _unverifiedChallenge.value = it }

                it.documents[0].reference.collection("tasks")
                    .orderBy("stage", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener {
                        val taskList = mutableListOf<Task>()
                        it.documents.forEach {
                            val task = it.toObject<Task>()
                            if (task != null) {
                                taskList.add(task)
                            }
                        }
                        _unverifiedTasks.value = taskList
                    }

            }

    }

    fun confirmChallenge(challengeId: String) {
        val db = Firebase.firestore
        val challenge = _unverifiedChallenge.value?.asMap()

        // add custom to challenges
        db.collection("challenges")
            .add(challenge as HashMap)
            .addOnSuccessListener { documentReference ->

                _unverifiedTasks.value?.forEach {
                    val task = it.asMap()
                    documentReference.collection("tasks")
                        .add(task)
                }

            }

        // delete unverified data
        db.collection("unverifiedCustoms").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener {
                it.documents[0].reference.delete()
            }

        // update user custom challenge status
        db.collection("users").whereEqualTo("id", _unverifiedChallenge.value?.author)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference.collection("customChallenges").whereEqualTo("id", challengeId)
                    .get()
                    .addOnSuccessListener {

                        it.documents[0].reference
                            .update("public", true)
                    }
            }
    }

    fun rejectChallenge(challengeId: String) {
        val db = Firebase.firestore

        // delete challenge in unverifiedChallenges
        db.collection("unverifiedCustoms").whereEqualTo("id", challengeId)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference
                    .delete()
                    .addOnSuccessListener {
                        _navigateUp.value = true
                    }
            }

        // update user custom challenge status
        db.collection("users").whereEqualTo("id", _unverifiedChallenge.value?.author)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference.collection("customChallenges").whereEqualTo("id", challengeId)
                    .get()
                    .addOnSuccessListener {

                        it.documents[0].reference
                            .update("upload", false)
                    }
            }
    }

    fun navigateUpCompleted() {
        _navigateUp.value = false
    }

    inline fun <reified T : Any> T.asMap() : Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }
}