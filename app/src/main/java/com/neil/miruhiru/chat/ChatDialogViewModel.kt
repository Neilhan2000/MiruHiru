package com.neil.miruhiru.chat

import android.app.Application
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Message
import com.neil.miruhiru.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ChatDialogViewModel(application: Application) : AndroidViewModel(application) {

    init {
        detectUserMessages()
    }

    private val viewModelApplication = application

    private val _messageList = MutableLiveData<List<MessageAdapter.MessageItem>>()
    val messageList: LiveData<List<MessageAdapter.MessageItem>>
        get() = _messageList

    private val _loadSuccess = MutableLiveData<Boolean>()
    val loadSuccess: LiveData<Boolean>
        get() = _loadSuccess

    private var userIdList = listOf<String>()
    val memberList = mutableListOf<User>()

    private val _isMainUser = MutableLiveData<Boolean>()
    val isMainUser: LiveData<Boolean>
        get() = _isMainUser

    private val _memberIcons = MutableLiveData<List<User>>()
    val memberIcons: LiveData<List<User>>
        get() = _memberIcons

    var event: Event? = Event()

    private fun detectUserMessages() {
        val db = Firebase.firestore


        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {

                val eventDocumentId = it.documents[0].id
                event = it.documents[0].toObject<Event>()
                event?.members?.let { userIdList = it }


                // load user
                for (userId in userIdList) {
                    db.collection("users").whereEqualTo("id", userId)
                        .get()
                        .addOnSuccessListener {
                            val user = it.documents[0].toObject<User>()
                            if (user != null) {
                                memberList.add(user)
                            }
     
                            if (memberList.size == event?.members?.size) {
                                _isMainUser.value = event?.members?.first() == UserManager.userId
                                // load message
                                db.collection("events").document(eventDocumentId).collection("messages").orderBy("time", Query.Direction.ASCENDING)
                                    .addSnapshotListener { messages, error ->
                                        val messageList :MutableList<MessageAdapter.MessageItem> = mutableListOf()

                                        messages?.let {
                                            for (m in messages.documents) {
                                                val message = m.toObject<Message>()
                                                message?.let {
                                                    if (message?.senderId == UserManager.userId) {
                                                        messageList.add(MessageAdapter.MessageItem.MyMessage(message = message))
                                                    } else {
                                                        messageList.add(MessageAdapter.MessageItem.OtherMessage(message = message))
                                                    }
                                                }

                                            }
                                        }
                                        _memberIcons.value = memberList
                                        _messageList.value = messageList

                                    }
                            }
                        }
                }

            }


    }

    fun sendMessages(message: String) {
        val db = Firebase.firestore
        val message = hashMapOf(
            "senderId" to UserManager.userId,
            "text" to message,
            "time" to Timestamp.now()
        )

        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {
                val eventDocumentId = it.documents[0].id

                db.collection("events").document(eventDocumentId).collection("messages")
                    .add(message)
            }

    }



    fun kick(userId: String) {
        val db = Firebase.firestore
        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {
                val eventDocumentId = it.documents[0].id
                val event = it.documents[0].toObject<Event>()


                // remove user from event
                db.collection("events").document(eventDocumentId)
                    .update("currentMembers", FieldValue.arrayRemove(userId))

                // remove user's currentEvent
                db.collection("users").whereEqualTo("id", userId)
                    .get()
                    .addOnSuccessListener {
                        val userDocumentId = it.documents[0].id

                        db.collection("users").document(userDocumentId)
                            .update("currentEvent", "")
                    }

                // we not remove user progress and currentEvent here, just let kicked user remove themself in TaskViewModel(fun detectUserKick)
            }
    }
}