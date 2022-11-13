package com.neil.miruhiru.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Message
import com.neil.miruhiru.data.User
import timber.log.Timber
import java.util.*

class ChatDialogViewModel : ViewModel() {

    init {
        detectUserMessages()
    }

    private val _messageList = MutableLiveData<List<MessageAdapter.MessageItem>>()
    val messageList: LiveData<List<MessageAdapter.MessageItem>>
        get() = _messageList

    private val _loadSuccess = MutableLiveData<Boolean>()
    val loadSuccess: LiveData<Boolean>
        get() = _loadSuccess

    private var userIdList = listOf<String>()
    val memberList = mutableListOf<User>()

    private fun detectUserMessages() {
        val db = Firebase.firestore



        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {
                val eventDocumentId = it.documents[0].id
                val event = it.documents[0].toObject<Event>()
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
}