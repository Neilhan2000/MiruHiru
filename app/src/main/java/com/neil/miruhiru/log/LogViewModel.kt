package com.neil.miruhiru.log

import android.annotation.SuppressLint
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.TypefaceCompatUtil.getTempFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.*
import timber.log.Timber
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class LogViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelApplication = application
    private lateinit var progressDialog: ProgressDialog

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean>
        get() = _uploadStatus

    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>>
        get() = _taskList

    private val _logList = MutableLiveData<List<Log>>()
    val logList: LiveData<List<Log>>
        get() = _logList

    private val _userInfoList = MutableLiveData<List<User>>()
    val userInfoList: LiveData<List<User>>
        get() = _userInfoList



    lateinit var imageUri: Uri
    var text = ""



    fun selectImage() {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
            .addFlags(FLAG_ACTIVITY_NEW_TASK)
        MainActivity.getInstanceFromViewModel()?.startActivityForResult(intent, 100)
    }

    fun uploadImageAndText() {
        progressDialog = ProgressDialog(MainActivity.getInstanceFromViewModel())
        progressDialog.setMessage("上傳中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

        if (this::imageUri.isInitialized) {
            storageReference.putFile(imageUri)
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener {
                        postTextAndPhotoToEvent(it)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(viewModelApplication, "上傳失敗，原因:${it.message}", Toast.LENGTH_SHORT).show()
                    if (progressDialog.isShowing) progressDialog.dismiss()
                    _uploadStatus.value = false
                }
        } else {
            postTextToEvent()
        }
    }

    private fun postTextAndPhotoToEvent(uri: Uri) {
        val db = Firebase.firestore
        var eventDocumentId = ""

        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {
                eventDocumentId = it.documents[0].id

                val log = hashMapOf(
                    "photo" to uri,
                    "text" to text,
                    "senderId" to UserManager.userId,
                    "time" to Timestamp.now(),
                    "stage" to UserManager.currentStage
                )
                db.collection("events").document(eventDocumentId).collection("logs")
                    .add(log)
                    .addOnSuccessListener {
                        Toast.makeText(viewModelApplication, "上傳成功", Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        _uploadStatus.value = true
                    }

            }

    }

    private fun postTextToEvent() {
        val db = Firebase.firestore
        var eventDocumentId = ""

        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {
                eventDocumentId = it.documents[0].id

                val log = hashMapOf(
                    "photo" to "",
                    "text" to text,
                    "senderId" to UserManager.userId,
                    "time" to Timestamp.now(),
                    "stage" to UserManager.currentStage
                )
                db.collection("events").document(eventDocumentId).collection("logs")
                    .add(log)
                    .addOnSuccessListener {
                        Toast.makeText(viewModelApplication, "上傳成功", Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        _uploadStatus.value = true
                    }

            }
    }

    private fun loadNewestCompletedEventLog() {
        val db = Firebase.firestore
        var eventDocumentId = ""
        val logList = mutableListOf<Log>()

        db.collection("events").whereEqualTo("id", UserManager.user.completedEvents.last())
            .get()
            .addOnSuccessListener {
                eventDocumentId = it.documents[0].id

                db.collection("events").document(eventDocumentId)
                    .collection("logs")
                    .get()
                    .addOnSuccessListener { result ->
                        for (log in result.documents) {
                            log.toObject<Log>()?.let { it ->
                                logList.add(it)
                            }
                        }
                        _logList.value = logList

                        loadUserInfo()
                    }
            }
    }

    // 所有參加者的資料
    private fun loadUserInfo() {
        val db = Firebase.firestore
        var event = Event()
        val userInfoList = mutableListOf<User>()

        db.collection("events").whereEqualTo("id", UserManager.user.completedEvents.last())
            .get()
            .addOnSuccessListener { result ->
                result?.documents?.get(0)?.toObject<Event>()?.let {
                    event = it
                }

                for (user in event.members) {
                    db.collection("users").whereEqualTo("id", user)
                        .get()
                        .addOnSuccessListener { result ->

                            val user = result.documents[0].toObject<User>()
                            Timber.i("user $user")
                            if (user != null) {
                                userInfoList.add(user)
                                _userInfoList.value = userInfoList

                            }
                        }
                }


            }

    }

    fun loadCompletedChallenge() {
        val db = Firebase.firestore
        var challengeId = ""
        var challengeDocumentId = ""
        val taskList = mutableListOf<Task>()

        db.collection("events").whereEqualTo("id", UserManager.user.completedEvents.last())
            .get()
            .addOnSuccessListener { result ->
                result.documents[0].toObject<Event>()?.challengeId?.let {
                    challengeId = it

                    db.collection("challenges").whereEqualTo("id", challengeId)
                        .get()
                        .addOnSuccessListener { result ->
                           challengeDocumentId = result.documents[0].id

                            db.collection("challenges").document(challengeDocumentId).collection("tasks")
                                .get()
                                .addOnSuccessListener { tasks ->
                                    tasks.forEach { task -> taskList.add(task.toObject<Task>()) }
                                    _taskList.value = taskList

                                    loadNewestCompletedEventLog()
                                }
                        }
                }
            }
    }
}