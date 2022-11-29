package com.neil.miruhiru.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Notification
import timber.log.Timber

class NotificationViewModel : ViewModel() {
    private val _notificationList = MutableLiveData<List<Notification>>()
    val notificationList: LiveData<List<Notification>>
        get() = _notificationList

    fun detectNotifications() {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                it.documents[0].reference.collection("notifications")
                    .addSnapshotListener { value, error ->
                        val notificationList = mutableListOf<Notification>()

                        value?.documents?.forEach {
                            val notification = it.toObject<Notification>()
                            if (notification != null) {
                                notificationList.add(notification)
                            }
                        }
                        _notificationList.value = notificationList
                    }

            }

    }
}