package com.neil.miruhiru.log

import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.*
import timber.log.Timber
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

    private val _timeSpent = MutableLiveData<Long>()
    val timeSpent: LiveData<Long>
        get() = _timeSpent


    lateinit var imageUri: Uri
    var text = ""



    fun selectImage() {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
            .addFlags(FLAG_ACTIVITY_NEW_TASK)
        MainActivity.getInstanceFromMainActivity()?.startActivityForResult(intent, 100)
    }

    fun uploadImageAndText() {
        progressDialog = ProgressDialog(MainActivity.getInstanceFromMainActivity())
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
                    "stage" to (UserManager.currentStage?.minus(1) ?: -1)
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
                    "stage" to (UserManager.currentStage?.minus(1) ?: -1)
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

    private fun loadNewestCompletedEventLog(eventId: String) {
        val db = Firebase.firestore
        var eventDocumentId = ""
        val logList = mutableListOf<Log>()

        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener {
                eventDocumentId = it.documents[0].id

                // get total time Spent
                val event = it.documents[0].toObject<Event>()
                val startTime = event?.startTime
                val endTime = event?.endTime
                val timeSpent = startTime?.seconds?.let { startTime -> endTime?.seconds?.minus(startTime) }
                Timber.i("start $startTime, end $endTime")
                timeSpent?.let { _timeSpent.value = it }

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

                        loadUserInfo(eventId)
                    }
            }
    }

    fun convertSecondsToHours(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val seconds = seconds % 60
        return String.format("總共花費 %02d 小時 %02d 分 %02d 秒", hours, minutes, seconds)
    }

    // all members info
    private fun loadUserInfo(eventId: String) {
        val db = Firebase.firestore
        var event = Event()
        val userInfoList = mutableListOf<User>()

        db.collection("events").whereEqualTo("id", eventId)
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

                                if (event.members.size == userInfoList.size) {
                                    _userInfoList.value = userInfoList
                                }
                            }
                        }

                }
            }

    }

    fun loadCompletedChallenge(eventId: String) {
        val db = Firebase.firestore
        var challengeId = ""
        var challengeDocumentId = ""
        val taskList = mutableListOf<Task>()

        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener { result ->
                val event = result.documents[0].toObject<Event>()
                event?.challengeId?.let {
                    challengeId = it

                    if (UserManager.isPersonal == false) {
                        db.collection("challenges").whereEqualTo("id", challengeId)
                            .get()
                            .addOnSuccessListener { result ->
                                challengeDocumentId = result.documents[0].id

                                db.collection("challenges").document(challengeDocumentId).collection("tasks")
                                    .get()
                                    .addOnSuccessListener { tasks ->
                                        tasks.forEach { task -> taskList.add(task.toObject<Task>()) }
                                        _taskList.value = taskList

                                        loadNewestCompletedEventLog(eventId)
                                    }
                            }
                    } else {
                        if (event.members.first() == UserManager.userId) {
                            db.collection("users").whereEqualTo("id", UserManager.userId)
                                .get()
                                .addOnSuccessListener {
                                    val userDocumentId = it.documents[0].id

                                    db.collection("users").document(userDocumentId).collection("customChallenges")
                                        .whereEqualTo("id", challengeId)
                                        .get()
                                        .addOnSuccessListener {
                                            val customDocumentId = it.documents[0].id

                                            db.collection("users").document(userDocumentId).collection("customChallenges")
                                                .document(customDocumentId).collection("tasks")
                                                .get()
                                                .addOnSuccessListener { tasks ->
                                                    tasks.forEach { task -> taskList.add(task.toObject<Task>()) }
                                                    _taskList.value = taskList

                                                    loadNewestCompletedEventLog(eventId)
                                                }
                                        }
                                }
                        } else {
                            val hostUserId = event.members.first()
                            db.collection("users").whereEqualTo("id", hostUserId)
                                .get()
                                .addOnSuccessListener {
                                    val userDocumentId = it.documents[0].id

                                    db.collection("users").document(userDocumentId).collection("customChallenges")
                                        .whereEqualTo("id", challengeId)
                                        .get()
                                        .addOnSuccessListener {
                                            val customDocumentId = it.documents[0].id

                                            db.collection("users").document(userDocumentId).collection("customChallenges")
                                                .document(customDocumentId).collection("tasks")
                                                .get()
                                                .addOnSuccessListener { tasks ->
                                                    tasks.forEach { task -> taskList.add(task.toObject<Task>()) }
                                                    _taskList.value = taskList

                                                    loadNewestCompletedEventLog(eventId)
                                                }
                                        }
                                }
                        }

                    }
                }
            }
    }
}