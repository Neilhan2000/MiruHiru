package com.neil.miruhiru

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.User

object UserManager {

    private const val USER_DATA = "user_data"
    private const val USER_ID = "user_token"
    private const val USER_CHALLENGE_ID = "user_challenge_id"
    private const val CURRENT_STAGE= "current_stage"
    private const val CUSTOM_CURRENT_STAGE = "custom_current_stage"
    private const val CUSTOM_TOTAL_STAGE = "custom_total_stage"
    private const val IS_PERSONAL = "is_personal"
    private const val NOTIFICATION = "notification"
    private const val MESSAGE = "message"



    private val _hasCurrentEvent = MutableLiveData<Event>()
    val hasCurrentEvent: LiveData<Event>
        get() = _hasCurrentEvent

    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User>
        get() = _userLiveData

    var user = User()
    var isPersonal: Boolean? = null
    get() = MiruHiruApplication.instance
    .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
    .getBoolean(IS_PERSONAL, false)
    set(value) {
        field = when (value) {
            null -> {
                MiruHiruApplication.instance
                    .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                    .remove(IS_PERSONAL)
                    .apply()
                null
            }
            else -> {
                MiruHiruApplication.instance
                    .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                    .putBoolean(IS_PERSONAL, value)
                    .apply()
                value
            }
        }
    }
    // this variable is used for updating log and cleaning progress of user current event
    var currentStage: Int? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getInt(CURRENT_STAGE, -1)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(CURRENT_STAGE)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putInt(CURRENT_STAGE, value)
                        .apply()
                    value
                }
            }
        }


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
    var userChallengeDocumentId: String? = null
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

    // For Custom Fragment(record current task stage and total stage)
    var unFinishEditingId = ""

    var customCurrentStage: Int? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getInt(CUSTOM_CURRENT_STAGE, -1)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(CUSTOM_CURRENT_STAGE)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putInt(CUSTOM_CURRENT_STAGE, value)
                        .apply()
                    value
                }
            }
        }
    var customTotalStage: Int? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getInt(CUSTOM_TOTAL_STAGE, -1)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(CUSTOM_TOTAL_STAGE)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putInt(CUSTOM_TOTAL_STAGE, value)
                        .apply()
                    value
                }
            }
        }

    // notifications
    var readNotifications: Int? = null
        get() = MiruHiruApplication.instance
            .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE)
            .getInt(NOTIFICATION, 0)
        set(value) {
            field = when (value) {
                null -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .remove(NOTIFICATION)
                        .apply()
                    null
                }
                else -> {
                    MiruHiruApplication.instance
                        .getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit()
                        .putInt(NOTIFICATION, value)
                        .apply()
                    value
                }
            }
        }

    // messages
    var readMessages = 0


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
//        _user.value = null
    }

    fun clearChallengeDocumentId() {
        userChallengeDocumentId = null
    }


    // get or update user info in local
    fun getUser() {

        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id" , userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = document.toObject<User>()
                    this.user = user
                    _userLiveData.value = user
                }

                // if has event, we load it
                if (user.currentEvent.isNotEmpty()) {
                    db.collection("events").whereEqualTo("id", user.currentEvent)
                        .get()
                        .addOnSuccessListener { result ->
                            _hasCurrentEvent.value = result.documents[0].toObject<Event>()
                        }
                }
            }

    }
}