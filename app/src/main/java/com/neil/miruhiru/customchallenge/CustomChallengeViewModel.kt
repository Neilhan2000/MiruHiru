package com.neil.miruhiru.customchallenge

import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import java.text.SimpleDateFormat
import java.util.*

class CustomChallengeViewModel : ViewModel() {

    val challenge = Challenge()

    private val _navigateToCustomDetailFragment = MutableLiveData<Boolean>()
    val navigateToCustomDetailFragment: LiveData<Boolean>
        get() = _navigateToCustomDetailFragment

    fun postChallenge() {

        val customChallenge = hashMapOf(
            "commentQuantity" to 0,
            "completedList" to listOf<String>(),
            "description" to challenge.description,
            "id" to getRandomString(),
            "image" to "",
            "isUpload" to false,
            "likeList" to listOf<String>(),
            "location" to GeoPoint(0.0, 0.0),
            "name" to challenge.name,
            "stage" to challenge.stage,
            "timeSpent" to challenge.timeSpent,
            "totalRating" to 0,
            "type" to challenge.type
        )

        val db = Firebase.firestore

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {
                val userDocumentId = it.documents[0].id

                db.collection("users").document(userDocumentId).collection("customChallenges")
                    .add(customChallenge)
                    .addOnSuccessListener {
                        val customChallengeDocumentId = it.id
                        storeAndPostChallengeImage(customChallengeDocumentId)
                    }
            }
    }

    private fun storeAndPostChallengeImage(documentId: String) {
        // store image
        if (challenge.image.isNotEmpty()) {
            val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)

            val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")

            storageReference.putFile(challenge.image.toUri())
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener {
                        // post image
                        postChallengeImage(it, documentId)
                    }
                }

        }
    }

    private fun postChallengeImage(uri: Uri, documentId: String) {

        val db = Firebase.firestore

        db.collection("users").document(documentId)
            .update("image", uri)
            .addOnSuccessListener { _navigateToCustomDetailFragment.value = true }

    }

    fun navigateToCustomDetailFragmentCompleted() {
        _navigateToCustomDetailFragment.value = false
    }

    private fun getRandomString() : String {
        return java.util.UUID.randomUUID().toString()
    }
}