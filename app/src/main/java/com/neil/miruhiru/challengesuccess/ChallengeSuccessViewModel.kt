package com.neil.miruhiru.challengesuccess

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import timber.log.Timber

class ChallengeSuccessViewModel : ViewModel() {

    private val _navigateToLogFragment = MutableLiveData<Boolean>()
    val navigateToLogFragment: LiveData<Boolean>
        get() = _navigateToLogFragment

    // post comment and clean local challenge document id
    fun postComment(rating: Float, text: String) {
        val db = Firebase.firestore
        val comment = hashMapOf(
            "rating" to rating,
            "text" to text,
            "userId" to UserManager.userId
        )

        UserManager.userChallengeDocumentId?.let { documentId ->
            db.collection("challenges").document(documentId).collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    UserManager.userChallengeDocumentId = null
                    _navigateToLogFragment.value = true
                }

        }
    }

    fun navigateToLogFragmentCompleted() {
        _navigateToLogFragment.value = false
    }
}