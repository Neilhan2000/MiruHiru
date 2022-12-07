package com.neil.miruhiru.customdetail

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.geojson.Point
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Feature
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.network.LoadingStatus
import com.neil.miruhiru.network.MapBoxApi
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CustomDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _navigateToCustomDetailFragment = MutableLiveData<Boolean>()
    val navigateToCustomDetailFragment: LiveData<Boolean>
        get() = _navigateToCustomDetailFragment

    private val _navigateToOverviewFragment = MutableLiveData<Boolean>()
    val navigateToOverviewFragment: LiveData<Boolean>
        get() = _navigateToOverviewFragment

    private val _isUnfinished = MutableLiveData<Boolean>()
    val isUnfinished: LiveData<Boolean>
        get() = _isUnfinished

    private val _continueEditingStage = MutableLiveData<Int>()
    val continueEditingStage: LiveData<Int>
        get() = _continueEditingStage

    private val viewModelApplication = application
    var task = Task()
    var customChallengeId = ""

    private val _isLastStage = MutableLiveData<Boolean>()
    val isLastStage: LiveData<Boolean>
        get() = _isLastStage

    private val _loadCurrentStage = MutableLiveData<Int>()
    val loadCurrentStage: LiveData<Int>
        get() = _loadCurrentStage

    private val _isUpdated = MutableLiveData<Boolean>()
    val isUpdated: LiveData<Boolean>
        get() = _isUpdated

    private val _featureList = MutableLiveData<List<Feature>>()
    val featureList: LiveData<List<Feature>>
        get() = _featureList

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)
    private lateinit var job: Job
    private var jobInitialized = false

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

    var originalTask = Task()

    fun setTaskLocation(point: Point) {
        task.location = GeoPoint(point.latitude(), point.longitude())
    }

    fun setOriginalTaskLocation(point: Point) {
        originalTask.location = GeoPoint(point.latitude(), point.longitude())
    }

    fun deleteTask() {
        task = Task(id = task.id, stage = task.stage)
    }

    fun setContinueStage(stage: Int) {
        _continueEditingStage.value = stage
    }

    fun postTask() {
        startLoading()

        val customTask = hashMapOf(
            "id" to UserManager.customCurrentStage.toString(),
            "location" to task.location,
            "answer" to task.answer,
            "guide" to task.guide,
            "image" to "",
            "introduction" to task.introduction,
            "question" to task.question,
            "stage" to UserManager.customCurrentStage,
            "name" to task.name
        )

        // post task content to user custom challenge
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
                            .add(customTask)
                            .addOnSuccessListener { storeAndPostTaskImage() }

                        Timber.i("post task first ${UserManager.customCurrentStage}")

                        // add challenge location(in the custom challenge fragment we don't have location so we add location here)
                        if (UserManager.customCurrentStage == 1) {
                            db.collection("users").document(userDocumentId).collection("customChallenges")
                                .document(customChallengeDocumentId)
                                .update("location", task.location)
                        }
                    }

            }

    }

    // post task image
    private fun storeAndPostTaskImage() {

        // store image
        if (task.image.isNotEmpty()) {
            val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)

            val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

            storageReference.putFile(task.image.toUri())
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener {
                        // post image
                        postTaskImage(it)
                    }
                }

        }
    }

    private fun postTaskImage(uri: Uri) {
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
                            .document(customChallengeDocumentId).collection("tasks").whereEqualTo("stage", UserManager.customCurrentStage)
                            .get()
                            .addOnSuccessListener {
                                val taskDocumentId = it.documents[0].id

                                db.collection("users").document(userDocumentId).collection("customChallenges")
                                    .document(customChallengeDocumentId).collection("tasks").document(taskDocumentId)
                                    .update("image", uri)
                                    .addOnSuccessListener {
                                        if (isLastStage.value == true) {
                                            UserManager.customCurrentStage = null
                                            UserManager.customTotalStage = null
                                            loadingCompleted()
                                            _navigateToOverviewFragment.value = true
                                            customChallengeEditingCompleted()
                                        } else {
                                            loadingCompleted()
                                            _navigateToCustomDetailFragment.value = true
                                        }
                                    }

                            }

                    }

            }

    }

    private fun customChallengeEditingCompleted() {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference.collection("customChallenges").whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {

                        it.documents[0].reference.update("finished", true)
                    }

            }

    }

    fun navigateToCustomDetailFragmentCompleted() {
        _navigateToCustomDetailFragment.value = false
    }

    fun navigateToOverviewFragmentCompleted() {
        _navigateToOverviewFragment.value = false
    }


    fun isInputValid(): Boolean {
        if (task.location.latitude == 0.0) {
            Toast.makeText(viewModelApplication, "還未選擇關卡地點歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.image.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未上傳關卡相片歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.name.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未輸入關卡名稱歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.introduction.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未輸入景點介紹歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.name.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未輸入關卡名稱歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.guide.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未輸入關卡提示歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.question.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未輸入關卡謎題歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (task.answer.isEmpty()) {
            Toast.makeText(viewModelApplication, "還未輸入謎題答案歐", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun isInputValidNoToast(): Boolean {
        if (task.location.latitude == 0.0) {
            return false
        } else if (task.image.isEmpty()) {
            return false
        } else if (task.name.isEmpty()) {
            return false
        } else if (task.introduction.isEmpty()) {
            return false
        } else if (task.name.isEmpty()) {
            return false
        } else if (task.guide.isEmpty()) {
            return false
        } else if (task.question.isEmpty()) {
            return false
        } else if (task.answer.isEmpty()) {
            return false
        }
        return true
    }

    fun loadFirstOrUnfinishedEditing() {
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
                        val challenge = it.documents[0].toObject<Challenge>()
                        UserManager.customTotalStage = challenge?.stage

                        db.collection("users").document(userDocumentId).collection("customChallenges")
                            .document(customChallengeDocumentId).collection("tasks")
                            .get()
                            .addOnSuccessListener {
                                UserManager.customCurrentStage = it.documents.size + 1

                                challenge?.stage?.let { challengeStage ->
                                    if (challengeStage > it.documents.size) {
                                        _isUnfinished.value = true
                                    }
                                }

                                if (challenge?.stage == 1) { _isLastStage.value = true }

                                // for continuing editing to set last stage
                                if (challenge?.stage == it.documents.size + 1) {
                                    _isLastStage.value = true
                                }
                                loadingCompleted()
                            }

                    }

            }

    }

    fun setLastStage() {
        _isLastStage.value = true
    }

    fun updateTask() {
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

                        // if stage equal to one we update the challenge location together
                        if (continueEditingStage.value == 1) {
                            it.documents[0].reference.update("location", task.location)
                        }

                        db.collection("users").document(userDocumentId)
                            .collection("customChallenges")
                            .document(customChallengeDocumentId).collection("tasks")
                            .whereEqualTo("id", task.id)
                            .get()
                            .addOnSuccessListener {
                                val customTask = hashMapOf(
                                    "id" to task.id,
                                    "location" to task.location,
                                    "answer" to task.answer,
                                    "guide" to task.guide,
                                    "image" to task.image,
                                    "introduction" to task.introduction,
                                    "question" to task.question,
                                    "stage" to task.stage,
                                    "name" to task.name
                                )

                                Timber.i("update")
                                it.documents[0].reference.update(customTask as Map<String, Any>)
                                    .addOnSuccessListener {
                                        // check if image the same, we don't want to upload same image again
                                        Timber.i("task image ${task.image} original image ${originalTask.image}")
                                        if (task.image != originalTask.image) {
                                            storeAndUpdateImage()
                                            originalTask.image = task.image
                                        } else {
                                            _isUpdated.value = true
                                            loadingCompleted()
                                        }
                                    }
                            }
                    }
            }
        UserManager.customCurrentStage = null
        UserManager.customTotalStage = null
    }

    fun needToUpdate() {
        _isUpdated.value = false
    }

    private fun storeAndUpdateImage() {

        Timber.i("store image")
        // store image
        if (task.image.isNotEmpty()) {
            // set file name
            val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)

            // get storage instance
            val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

            // put image file to storage
            storageReference.putFile(task.image.toUri())
                .addOnSuccessListener {
                    // put file success and we get the image uri
                    storageReference.downloadUrl.addOnSuccessListener {
                        // then we update this uri to firebase data base
                        updateImage(it)
                    }
                }

        }
    }

    private fun updateImage(uri: Uri) {
        val db = Firebase.firestore

        Timber.i("update image")
        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                val userDocumentId = it.documents[0].id

                db.collection("users").document(userDocumentId).collection("customChallenges")
                    .whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {
                        val customChallengeDocumentId = it.documents[0].id

                        db.collection("users").document(userDocumentId)
                            .collection("customChallenges")
                            .document(customChallengeDocumentId).collection("tasks")
                            .whereEqualTo("id", task.id)
                            .get()
                            .addOnSuccessListener {
                                it.documents[0].reference.update("image", uri)
                                    .addOnSuccessListener {
                                        _isUpdated.value = true
                                        loadingCompleted()
                                    }
                            }
                    }
            }
    }

    fun cleanCustomChallenge(customChallengeId: String) {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference.collection("customChallenges").whereEqualTo("id", customChallengeId)
                    .get()
                    .addOnSuccessListener {

                        it.documents[0].reference.delete()
                    }

            }

    }

    fun searchPlace(place: String, limit: Int, accessToken: String) {

        if (jobInitialized) {
            job.cancel()
        }
        coroutineScope.launch {
            job = launch {
                jobInitialized = true
                val result = MapBoxApi.retrofitService.getProductList("$place.json",limit, accessToken)
                if (result.features.isNotEmpty()) {
                    val featureList = mutableListOf<Feature>()
                    result.features.forEach { feature ->
                        featureList.add(feature)
                    }
                    _featureList.value = featureList
                }
            }
        }
    }

    fun cleanSearchResult() {
        _featureList.value = mutableListOf()
    }
}