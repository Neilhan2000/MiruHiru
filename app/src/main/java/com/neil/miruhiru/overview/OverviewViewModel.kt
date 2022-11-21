package com.neil.miruhiru.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Task
import timber.log.Timber
import java.lang.reflect.Field

class OverviewViewModel : ViewModel() {

    var customChallengeId = ""

    private val _customTaskList = MutableLiveData<List<Task>>()
    val customTaskList: LiveData<List<Task>>
        get() = _customTaskList

    private val _resetStartButton = MutableLiveData<Boolean>()
    val resetStartButton: LiveData<Boolean>
        get() = _resetStartButton

    fun loadCustomTasks() {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                val userDocumentId = it.documents[0].id

                db.collection("users").document(userDocumentId).collection("customChallenges")
                    .whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {
                        val customChallengeDocumentId = it.documents[0].id

                        db.collection("users").document(userDocumentId).collection("customChallenges")
                            .document(customChallengeDocumentId).collection("tasks").orderBy("stage", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener { result ->
                                val customTaskList = mutableListOf<Task>()
                                for (task in result) {
                                    val customTask = task.toObject<Task>()
                                    customTaskList.add(customTask)
                                }
                                _customTaskList.value = customTaskList
                            }
                    }
            }
    }

    fun updateCustomChallenge() {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                val userDocumentId = it.documents[0].id

                db.collection("users").document(userDocumentId).collection("customChallenges")
                    .whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {
                        val customChallengeDocumentId = it.documents[0].id

                        db.collection("users").document(userDocumentId).collection("customChallenges")
                            .document(customChallengeDocumentId).collection("tasks")
                            .get()
                            .addOnSuccessListener { result ->
                                for (document in result.documents) {
                                    val remoteTask = document.toObject<Task>()
                                    _customTaskList.value?.forEach { localTask ->
                                        if (remoteTask?.id == localTask.id) {
                                            document.reference.update("stage", localTask.stage)
                                                .addOnSuccessListener {
                                                    if (remoteTask.stage == result.documents.size) {
                                                        _resetStartButton.value = true
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                    }
            }
    }

    fun resetStartButtonCompleted() {
        _resetStartButton.value = false
    }

    fun resetTasksStages(position: Int) {
        // called in adapter onBind method
        _customTaskList.value?.let { customTaskList ->
            customTaskList[position].stage = position + 1
        }
        Timber.i("list = ${_customTaskList.value}")
    }
}