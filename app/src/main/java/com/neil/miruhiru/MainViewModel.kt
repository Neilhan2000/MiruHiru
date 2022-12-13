package com.neil.miruhiru

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.*
import com.neil.miruhiru.data.source.MiruHiruRepository
import com.neil.miruhiru.util.Util.getString
import kotlinx.coroutines.*
import timber.log.Timber

class MainViewModel(private val repository: MiruHiruRepository): ViewModel() {

    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    private var _notificationList = MutableLiveData<List<Notification>>()
    val notificationList: LiveData<List<Notification>>
        get() = _notificationList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    fun cleanEventSingle() {

        coroutineScope.launch {

            val resultEvent = repository.cleanEventSingle()
            when (resultEvent) {
                is Result.Success -> {
                    _error.value = null
                }
                is Result.Fail -> {
                    _error.value = resultEvent.error
                }
                is Result.Error -> {
                    _error.value = resultEvent.exception.toString()
                }
                else -> {
                    _error.value = getString(R.string.loading_fail)
                }
            }

            if (resultEvent is Result.Success) {

                when (val resultClean = repository.cleanUserCurrentEvent(resultEvent.data)) {
                    is Result.Success -> {
                        _error.value = null
                    }
                    is Result.Fail -> {
                        _error.value = resultClean.error
                    }
                    is Result.Error -> {
                        _error.value = resultClean.exception.toString()
                    }
                    else -> {
                        _error.value = getString(R.string.loading_fail)
                    }
                }
            }
        }
    }

    fun cleanEventMultiple() {

        coroutineScope.launch {

            val resultUser = repository.getUserDocumentId()
            when (resultUser) {
                is Result.Success -> {
                    _error.value = null
                }
                is Result.Fail -> {
                    _error.value = resultUser.error
                }
                is Result.Error -> {
                    _error.value = resultUser.exception.toString()
                }
                else -> {
                    _error.value = getString(R.string.loading_fail)
                }
            }

            if (resultUser is Result.Success) {

                when (val resultEvent = repository.removeUserProgress(resultUser.data)) {
                    is Result.Success -> {
                        _error.value = null
                    }
                    is Result.Fail -> {
                        _error.value = resultEvent.error
                    }
                    is Result.Error -> {
                        _error.value = resultEvent.exception.toString()
                    }
                    else -> {
                        _error.value = getString(R.string.loading_fail)
                    }
                }

                when (val resultClean = repository.cleanUserCurrentEvent(resultUser.data)) {
                    is Result.Success -> {
                        _error.value = null
                    }
                    is Result.Fail -> {
                        _error.value = resultClean.error
                    }
                    is Result.Error -> {
                        _error.value = resultClean.exception.toString()
                    }
                    else -> {
                        _error.value = getString(R.string.loading_fail)
                    }
                }
            }
        }
    }

//    fun detectNotifications() {
//
//        coroutineScope.launch {
//
//            val resultUser = repository.getUserDocumentId()
//            when (resultUser) {
//                is Result.Success -> {
//                    _error.value = null
//                }
//                is Result.Fail -> {
//                    _error.value = resultUser.error
//                }
//                is Result.Error -> {
//                    _error.value = resultUser.exception.toString()
//                }
//                else -> {
//                    _error.value = getString(R.string.loading_fail)
//                }
//            }
//
//            if (resultUser is Result.Success) {
//
//                val resultNotification = repository.detectNotifications(resultUser.data)
//                _error.value = null
//                _notificationList = resultNotification
//            }
//        }
//    }

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
                        Timber.i("error $error")
                    }

            }

    }

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }
}