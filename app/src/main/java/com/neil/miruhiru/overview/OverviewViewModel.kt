package com.neil.miruhiru.overview

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Task
import timber.log.Timber

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelApplication = application

    var customChallengeId = ""

    private val _customTaskList = MutableLiveData<List<Task>>()
    val customTaskList: LiveData<List<Task>>
        get() = _customTaskList

    private val _editingCompleted = MutableLiveData<Boolean>()
    val editingCompleted: LiveData<Boolean>
        get() = _editingCompleted

    private val _resetStartButton = MutableLiveData<Boolean>()
    val resetStartButton: LiveData<Boolean>
        get() = _resetStartButton

    private val _uploadToBeVerified = MutableLiveData<Boolean>()
    val uploadToBeVerified: LiveData<Boolean>
        get() = _uploadToBeVerified

    private var challenge: Challenge? = Challenge()

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
                        challenge = it.documents[0].toObject<Challenge>()

                        db.collection("users").document(userDocumentId).collection("customChallenges")
                            .document(customChallengeDocumentId).collection("tasks").orderBy("stage", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener { result ->
                                val customTaskList = mutableListOf<Task>()
                                for (task in result) {
                                    val customTask = task.toObject<Task>()
                                    customTaskList.add(customTask)
                                }

                                if (challenge?.upload == true) {
                                    _uploadToBeVerified.value = true
                                    _customTaskList.value = customTaskList
                                } else {
                                    _editingCompleted.value = challenge?.stage == customTaskList.size
                                    _customTaskList.value = customTaskList
                                }
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

    fun uploadCustomChallengeBeVerified() {

        val customChallenge = hashMapOf(
            "description" to challenge?.description,
            "id" to challenge?.id,
            "image" to challenge?.image,
            "upload" to true,
            "likeList" to challenge?.likeList,
            "location" to challenge?.location,
            "name" to challenge?.name,
            "stage" to challenge?.stage,
            "timeSpent" to challenge?.timeSpent,
            "totalRating" to challenge?.totalRating,
            "type" to challenge?.type,
            "completedList" to challenge?.completedList,
            "commentQuantity" to challenge?.commentQuantity,
            "createdTime" to challenge?.createdTime,
            "finished" to challenge?.finished
        )

        val db = Firebase.firestore

        // upload challenge
        db.collection("unverifiedCustoms")
            .add(customChallenge)
            .addOnSuccessListener {

                // upload tasks
                customTaskList?.value?.let { customTaskListValue ->
                    var count = 0
                    for (customTask in customTaskListValue) {
                        val task = hashMapOf(
                            "location" to customTask.location,
                            "answer" to customTask.answer,
                            "guide" to customTask.guide,
                            "id" to customTask.id,
                            "image" to customTask.image,
                            "introduction" to customTask.introduction,
                            "question" to customTask.question,
                            "stage" to customTask.stage,
                            "name" to customTask.name
                        )
                        it.collection("tasks")
                            .add(task)
                            .addOnSuccessListener {
                                count ++
                                if (count == customTaskListValue.size) {
                                    setUserCustomChallengeIsUploadTrue()
                                }
                            }
                    }
                }
            }
    }

    private fun setUserCustomChallengeIsUploadTrue() {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference.collection("customChallenges").whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {

                        it.documents[0].reference
                            .update("upload", true)
                            .addOnSuccessListener {
                                _uploadToBeVerified.value = true
                                Toast.makeText(viewModelApplication, viewModelApplication.getString(R.string.upload_challenge_success_toast), Toast.LENGTH_LONG).show()
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