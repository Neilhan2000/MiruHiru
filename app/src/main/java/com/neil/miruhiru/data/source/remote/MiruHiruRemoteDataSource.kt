package com.neil.miruhiru.data.source.remote

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.*
import com.neil.miruhiru.data.source.MiruHiruDataSource
import com.neil.miruhiru.util.Util.getString
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object MiruHiruRemoteDataSource : MiruHiruDataSource {

    private const val PATH_USERS = "users"
    private const val PATH_EVENTS = "events"
    private const val PATH_NOTIFICATIONS = "notifications"
    private const val KEY_ID = "id"
    private const val KEY_CURRENT_EVENT = "currentEvent"
    private const val KEY_EVENT = "events"
    private const val KEY_PROGRESS = "progress"

    override suspend fun cleanEventSingle(): Result<String> = suspendCoroutine { continuation ->

        val db = Firebase.firestore

        db.collection(PATH_USERS).whereEqualTo(KEY_ID, UserManager.userId)
            .get()
            .addOnSuccessListener {

                if (it.documents.isNotEmpty()) {
                    val userDocumentId = it.documents[0].id
                    continuation.resume(Result.Success(userDocumentId))
                }
            }
            .addOnFailureListener {

                Timber.i(getString(R.string.error_getting_document) + it.message)
                continuation.resume(Result.Error(it))
            }

    }



    override suspend fun cleanEventMultiple(): Result<String> = suspendCoroutine { continuation ->
        val db = Firebase.firestore

        db.collection(PATH_USERS).whereEqualTo(KEY_ID, UserManager.userId)
            .get()
            .addOnSuccessListener {
                val user = it.documents[0].toObject<User>()
                val userDocumentId = it.documents[0].id

                db.collection(KEY_EVENT).whereEqualTo(KEY_ID, user?.currentEvent)
                    .get()
                    .addOnSuccessListener {

                        val event = it.documents[0].toObject<Event>()
                        val progress = event?.progress as MutableList<Int>
                        progress.remove(UserManager.currentStage)

                        // remove progress
                        it.documents[0].reference
                            .update(KEY_PROGRESS, progress)
                            .addOnSuccessListener {
                                UserManager.currentStage = null
                            }
                            .addOnFailureListener {

                            }

                        // remove user current event
                        db.collection(PATH_USERS).document(userDocumentId)
                            .update(KEY_CURRENT_EVENT, "")
                            .addOnSuccessListener {
                                UserManager.getUser()
                            }
                            .addOnFailureListener {

                            }

                    }
                    .addOnFailureListener {

                    }

            }
            .addOnFailureListener {

            }

    }

    override suspend fun cleanUserCurrentEvent(userDocumentId: String): Result<Boolean> =
        suspendCoroutine { continuation ->

        val db = Firebase.firestore
        db.collection(PATH_USERS).document(userDocumentId)
            .update(KEY_CURRENT_EVENT, "")
            .addOnSuccessListener {

                UserManager.getUser()
                continuation.resume(Result.Success(true))
            }
            .addOnFailureListener {

                Timber.i(getString(R.string.error_getting_document) + it.message)
                continuation.resume(Result.Error(it))
            }

    }

    override suspend fun getUserDocumentId(): Result<String> = suspendCoroutine { continuation ->
        val db = Firebase.firestore

        db.collection(PATH_USERS).whereEqualTo(KEY_ID, UserManager.userId)
            .get()
            .addOnSuccessListener {

                val userDocumentId = it.documents[0].id
                continuation.resume(Result.Success(userDocumentId))
            }
            .addOnFailureListener {

                Timber.i(getString(R.string.error_getting_document) + it.message)
                continuation.resume(Result.Error(it))
            }

    }

    override suspend fun removeUserProgress(userDocumentId: String): Result<Boolean>
    = suspendCoroutine { continuation ->
        val db = Firebase.firestore

        db.collection(PATH_USERS).document(userDocumentId)
            .get()
            .addOnSuccessListener {

                if (it.data != null) {
                    val user = it.toObject<User>()
                    db.collection(PATH_EVENTS).whereEqualTo(KEY_ID, user?.currentEvent)
                        .get()
                        .addOnSuccessListener {

                            val event = it.documents[0].toObject<Event>()
                            val progress = event?.progress as MutableList<Int>
                            progress.remove(UserManager.currentStage)

                            // remove progress
                            it.documents[0].reference
                                .update(KEY_PROGRESS, progress)
                                .addOnSuccessListener {

                                    UserManager.currentStage = null
                                    continuation.resume(Result.Success(true))
                                }
                                .addOnFailureListener {

                                    Timber.i(getString(R.string.error_getting_document) + it.message)
                                    continuation.resume(Result.Error(it))
                                }

                        }
                        .addOnFailureListener {

                            Timber.i(getString(R.string.error_getting_document) + it.message)
                            continuation.resume(Result.Error(it))
                        }

                }
            }
            .addOnFailureListener {

                Timber.i(getString(R.string.error_getting_document) + it.message)
                continuation.resume(Result.Error(it))
            }

    }

    override fun detectNotifications(userDocumentId: String): MutableLiveData<List<Notification>> {
        val db = Firebase.firestore
        val liveData = MutableLiveData<List<Notification>>()
        val notificationList = mutableListOf<Notification>()

        db.collection(PATH_USERS).document(userDocumentId).collection(PATH_NOTIFICATIONS)
            .addSnapshotListener { value, exception ->

                if (exception != null) {
                    Timber.i(getString(R.string.error_getting_document) + exception.message)
                } else {

                    value?.documents?.forEach {
                        val notification = it.toObject<Notification>()
                        if (notification != null) {
                            notificationList.add(notification)
                        }
                    }
                }
                liveData.value = notificationList
            }
        return liveData
    }
}