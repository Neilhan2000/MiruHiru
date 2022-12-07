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
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber
import kotlin.reflect.full.memberProperties

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

    fun loadCustomTasks() {
        startLoading()

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
                                    loadingCompleted()
                                } else {
                                    _editingCompleted.value = challenge?.stage == customTaskList.size
                                    _customTaskList.value = customTaskList
                                    loadingCompleted()
                                }
                            }
                    }
            }
    }

    fun updateCustomChallenge() {
        startLoading()

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
                                                        loadingCompleted()
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
        startLoading()

        val customChallenge = challenge?.asMap()
        val db = Firebase.firestore

        // upload challenge
        db.collection("unverifiedCustoms")
            .add(customChallenge as HashMap)
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
                                loadingCompleted()
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

    inline fun <reified T : Any> T.asMap() : Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }
}