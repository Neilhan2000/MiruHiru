package com.neil.miruhiru.customchallenge

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.customdetail.item.BottomSheetViewModel
import com.neil.miruhiru.customdetail.item.BottomStepFragment
import com.neil.miruhiru.data.Challenge
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CustomChallengeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val GALLERY_CODE = 1
    }

    val challenge = Challenge(stage = 1, timeSpent = 0)
    var customChallengeId = getRandomString()

    private val viewModelApplication = application
    private val _navigateToCustomDetailFragment = MutableLiveData<Boolean>()
    val navigateToCustomDetailFragment: LiveData<Boolean>
        get() = _navigateToCustomDetailFragment

    fun postChallenge() {

        val customChallenge = hashMapOf(
            "commentQuantity" to 0,
            "completedList" to listOf<String>(),
            "description" to challenge.description,
            "id" to customChallengeId,
            "image" to "",
            "upload" to false,
            "likeList" to listOf<String>(),
            "location" to GeoPoint(0.0, 0.0),
            "name" to challenge.name,
            "stage" to challenge.stage,
            "timeSpent" to challenge.timeSpent,
            "totalRating" to 0,
            "type" to challenge.type,
            "createdTime" to Timestamp.now(),
            "finished" to false
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
                        storeAndPostChallengeImage(userDocumentId, customChallengeDocumentId)
                    }
            }
    }

    private fun storeAndPostChallengeImage(userDocumentId: String, customDocumentId: String) {

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
                        postChallengeImage(it, userDocumentId, customDocumentId)
                    }
                }

        }
    }

    private fun postChallengeImage(uri: Uri, userDocumentId: String, customDocumentId: String) {

        val db = Firebase.firestore

        db.collection("users").document(userDocumentId).collection("customChallenges")
            .document(customDocumentId)
            .update("image", uri)
            .addOnSuccessListener { _navigateToCustomDetailFragment.value = true }

    }

    fun navigateToCustomDetailFragmentCompleted() {
        _navigateToCustomDetailFragment.value = false
    }

    private fun getRandomString() : String {
        return java.util.UUID.randomUUID().toString()
    }

    fun selectImage(fragment: CustomChallengeFragment) {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        fragment.startActivityForResult(Intent.createChooser(intent,"gallery"), GALLERY_CODE)
    }

    fun setTaskImage(uri: Uri) {
        challenge.image = uri.toString()
    }

    fun isInputValid(): Boolean {
        if (challenge.name.isEmpty()) {
            Toast.makeText(viewModelApplication, "你還沒輸入挑戰標題歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (challenge.description.isEmpty()) {
            Toast.makeText(viewModelApplication, "你還沒輸入挑戰描述歐", Toast.LENGTH_SHORT).show()
            return false
        }  else if (challenge.timeSpent == 0L) {
            Toast.makeText(viewModelApplication, "你還沒輸入花費時間歐", Toast.LENGTH_SHORT).show()
            return false
        } else if (challenge.type.isEmpty()) {
            Toast.makeText(viewModelApplication, "你還沒選擇挑戰標籤歐", Toast.LENGTH_SHORT).show()
            return false
        }  else if (challenge.image.isEmpty()) {
            Toast.makeText(viewModelApplication, "你還沒上傳挑戰封面歐", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}