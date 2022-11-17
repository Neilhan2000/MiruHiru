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
import com.neil.miruhiru.data.Task
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

    private val viewModelApplication = application
    var task = Task()
    var customChallengeId = ""

    private val _isLastStage = MutableLiveData<Boolean>()
    val isLastStage: LiveData<Boolean>
        get() = _isLastStage

    fun setTaskLocation(point: Point) {
        task.location = GeoPoint(point.latitude(), point.longitude())
    }

    fun deleteTask() {
        task = Task()
    }

    fun postTask() {

        val customTask = hashMapOf(
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

                        // add challenge location
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
                                            _navigateToOverviewFragment.value = true
                                        } else {
                                            _navigateToCustomDetailFragment.value = true
                                        }
                                    }
                            }
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


    fun loadFirstOrUnfinishedEditing() {
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
                                if (challenge?.stage == 1) { _isLastStage.value = true }
                            }
                    }

            }
    }

    fun setLastStage() {
        _isLastStage.value = true
    }

}