package com.neil.miruhiru.chat

import android.app.Application
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
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.Message
import com.neil.miruhiru.data.User
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
                                _isMainUser.value = event.members.first() == UserManager.userId
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

    fun kickUser() {
        val db = Firebase.firestore

        val users = mutableListOf<String>()

        for (user in memberList) {
            if (user.id != UserManager.userId) {
                users.add(user.name)
            }
        }

        val builder = AlertDialog.Builder(viewModelApplication)
        builder.setTitle("選擇要移除的使用者")
        builder.setSingleChoiceItems(users.toTypedArray(), -1) { dialog, which ->
            kick(users[which])
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()


    }

    fun kick(userId: String) {
        val db = Firebase.firestore
        db.collection("events").whereEqualTo("id", UserManager.user.currentEvent)
            .get()
            .addOnSuccessListener {
                val eventDocumentId = it.documents[0].id

                db.collection("events").document(eventDocumentId)
                    .update("currentMembers", FieldValue.arrayRemove(userId))
            }
    }
}