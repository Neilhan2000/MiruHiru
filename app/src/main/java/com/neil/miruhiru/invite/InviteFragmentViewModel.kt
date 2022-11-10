package com.neil.miruhiru.invite

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mapbox.geojson.Point
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.User
import timber.log.Timber

class InviteFragmentViewModel: ViewModel() {


    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>>
        get() = _userList

    private val _navigateToTaskFragment = MutableLiveData<Boolean>()
    val navigateToTaskFragment: LiveData<Boolean>
        get() = _navigateToTaskFragment

    var mainUser = ""

    fun navigateToTaskFragmentCompleted() {
        _navigateToTaskFragment.value = false
    }

    fun detectUserJoin(eventId: String) {
        val db = Firebase.firestore
        db.collection("events").whereEqualTo("id", eventId)
            .addSnapshotListener { value, error ->
                val event = value?.documents?.get(0)?.toObject<Event>()
                val userIdList = mutableListOf<String>()
                event?.members?.let { for (userId in event.members) {
                    userIdList.add(userId)
                } }
                mainUser = userIdList[0]
                Timber.i("userid list $userIdList")
                loadUserData(userIdList)
            }
    }

    private fun loadUserData(userIdList: List<String>) {
        val db = Firebase.firestore
        val userList = mutableListOf<User>()
        for (userId in userIdList) {

            db.collection("users").whereEqualTo("id", userId)
                .get().addOnSuccessListener { result ->
                    val user = result.documents.get(0).toObject<User>()


                    user?.let { userList.add(user) }
                    _userList.value = userList
                }
        }
    }

    fun eventStart(eventId: String) {
        val db = Firebase.firestore
        var eventDocumentId = ""
        db.collection("events").whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener { result ->
                eventDocumentId = result.documents[0].id
                db.collection("events").document(eventDocumentId)
                    .update("status", "start")
            }
    }

    fun detectEventStart(eventId: String) {
        val db = Firebase.firestore

        db.collection("events").whereEqualTo("id", eventId)
            .addSnapshotListener { value, error ->
                val event = value?.documents?.let { it[0].toObject<Event>() }
                if (event?.status == "start") {
                    _navigateToTaskFragment.value = true
                }
            }
    }


}