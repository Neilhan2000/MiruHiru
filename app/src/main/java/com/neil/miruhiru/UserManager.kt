package com.neil.miruhiru

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.User

object UserManager {

    private const val USER_DATA = "user_data"
    private const val USER_ID = "user_token"
    private const val USER_CHALLENGE_ID = "user_challenge_id"
    private const val CURRENT_EVENT_ID = "current_event_id"

    private val _user = MutableLiveData<User>()

    val user: LiveData<User>
        get() = _user

    // id or userToken
    var userId: String? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getString(USER_ID, null)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(USER_ID)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putString(USER_ID, value)
                        .apply()
                    value
                }
            }
        }

    // To check if user has uncompleted challenge
    var userChallengeId: String? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getString(USER_CHALLENGE_ID, null)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(USER_CHALLENGE_ID)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putString(USER_CHALLENGE_ID, value)
                        .apply()
                    value
                }
            }
        }

    var currentEventId: String? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getString(CURRENT_EVENT_ID, null)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(CURRENT_EVENT_ID)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putString(CURRENT_EVENT_ID, value)
                        .apply()
                    value
                }
            }
        }

    /**
     * It can be use to check login status directly
     */
    val isLoggedIn: Boolean
        get() = userId != null

    /**
     * Clear the [userId] and the [user]/[_user] data
     */
    fun clear() {
        userId = null
        _user.value = null
    }

    fun clearChallengeId() {
        userChallengeId = null
    }

    fun clearCurrentEventId() {
        currentEventId = null
    }


    // get or update user info in local
    fun getUser() {

        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id" , userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject<User>()
                    _user.value = user
                }
            }
            .addOnFailureListener { exception ->
                Log.i("neil", "Error getting documents.", exception)
            }
    }
}