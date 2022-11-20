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
import timber.log.Timber

class MyCustomViewModel : ViewModel() {

    private val _myCustomList = MutableLiveData<List<Challenge>>()
    val myCustomList: LiveData<List<Challenge>>
        get() = _myCustomList

    fun loadMyCustom() {
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
                    }
            }
    }


}