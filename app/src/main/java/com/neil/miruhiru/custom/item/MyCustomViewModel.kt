package com.neil.miruhiru.custom.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber

class MyCustomViewModel : ViewModel() {

    private val _myCustomList = MutableLiveData<List<Challenge>>()
    val myCustomList: LiveData<List<Challenge>>
        get() = _myCustomList

    private val _showDeleteText = MutableLiveData<Boolean>()
    val showDeleteText: LiveData<Boolean>
        get() = _showDeleteText

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

    var selectedPositions = mutableListOf<Int>()
    var isLongClick = false

    fun showDeleteText() {
        _showDeleteText.value = true
    }
    fun hideDeleteText() {
        _showDeleteText.value = false
    }

    fun cancelLongClick() {
        isLongClick = false
    }
    fun cleanSelectedPositions() {
        selectedPositions.removeAll(selectedPositions)
    }
    fun deleteSelectedItems() {
        startLoading()

        val db = Firebase.firestore
        val myCustomList = _myCustomList.value as MutableList
        val remainItemListSize = _myCustomList.value?.size?.minus(selectedPositions.size)

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                selectedPositions.forEach { position ->
                    Timber.i("delete $position")
                    it.documents[0].reference.collection("customChallenges")
                        .whereEqualTo("id", _myCustomList.value?.get(position)?.id)
                        .get()
                        .addOnSuccessListener {

                            it.documents[0].reference
                                .delete()
                                .addOnSuccessListener {
                                    myCustomList.removeAt(position)
                                    if (myCustomList.size == remainItemListSize) {
                                        cleanSelectedPositions()
                                        cancelLongClick()
                                        _myCustomList.value = myCustomList
                                        loadingCompleted()
                                    }
                                }

                        }

                }
            }

    }

    fun loadMyCustom() {
        startLoading()

        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                val userDocumentId = it.documents[0].id

                db.collection("users").document(userDocumentId).collection("customChallenges")
                    .orderBy("createdTime", Query.Direction.DESCENDING)
                    .get().addOnSuccessListener {
                        val myCustomList = mutableListOf<Challenge>()

                        it.documents.forEach {
                            val challenge = it.toObject<Challenge>()
                            if (challenge != null) {
                                myCustomList.add(challenge)
                            }
                        }
                        _myCustomList.value = myCustomList
                        loadingCompleted()
                    }
            }
    }

//    fun deleteSelectedItems() {
//        _isDeleteCompleted.value = true
//    }

}