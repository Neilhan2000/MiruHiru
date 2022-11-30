package com.neil.miruhiru.challengesuccess

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber

class ChallengeSuccessViewModel : ViewModel() {

    private val _navigateToLogFragment = MutableLiveData<Boolean>()
    val navigateToLogFragment: LiveData<Boolean>
        get() = _navigateToLogFragment

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

    // post comment and clean local challenge document id
    fun postComment(rating: Float, text: String) {
        startLoading()

        val db = Firebase.firestore
        val comment = hashMapOf(
            "rating" to rating,
            "text" to text,
            "userId" to UserManager.userId
        )

        // post comment to challenge
        UserManager.userChallengeDocumentId?.let { documentId ->
            db.collection("challenges").document(documentId).collection("comments")
                .add(comment)
                .addOnSuccessListener {

                    // increase challenge comment quantity
                    db.collection("challenges").document(documentId)
                        .update("commentQuantity", FieldValue.increment(1))
                        .addOnSuccessListener {

                            // calculate challenge total rating and update
                            db.collection("challenges").document(documentId)
                                .get()
                                .addOnSuccessListener {
                                    val challenge = it.toObject<Challenge>()
                                    var oldQuantity = 0F
                                    var oldTotal = 0F
                                    var newTotal = 0F
                                    challenge?.commentQuantity?.minus(1)?.let { oldQuantity = it.toFloat() }
                                    challenge?.totalRating?.let { oldTotal = (it * oldQuantity) }
                                    challenge?.commentQuantity?.toFloat()?.let { newTotal = (oldTotal + rating) / it }

                                    db.collection("challenges").document(documentId)
                                        .update("totalRating", newTotal)
                                        .addOnSuccessListener {

                                            UserManager.userChallengeDocumentId = null
                                            _navigateToLogFragment.value = true
                                            loadingCompleted()
                                        }

                                }
                        }


                }

        }
    }

    fun navigateToLogFragmentCompleted() {
        _navigateToLogFragment.value = false
    }
}