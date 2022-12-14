package com.neil.miruhiru.signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.MiruHiruApplication
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.User
import timber.log.Timber

class SignInViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val GOOGLE_SIGN_IN_CODE = 3
    }

    private val viewModelApplication = application

    private val _navigateToExploreFragment = MutableLiveData<Boolean>()
    val navigateToExploreFragment: LiveData<Boolean>
        get() = _navigateToExploreFragment

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(
        viewModelApplication, googleSignInOptions)

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            checkHasAccount(account)

        } catch (e: ApiException) {
            Timber.i("signInResult:failed code = ${e.statusCode}")
        }
    }

    fun signIn(fragment: SignInFragment) {
        val signInIntent = googleSignInClient.signInIntent
        fragment.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE)
    }

    // check if user already register MiruHiru account
    private fun checkHasAccount(account: GoogleSignInAccount): Boolean? {
        val db = Firebase.firestore
        var hasAccount: Boolean? = null

        db.collection("users").whereEqualTo("id", account.email)
            .get()
            .addOnSuccessListener { result ->
                hasAccount =  result.size() == 1

                if (hasAccount == true) {
                    updateAccount(account)
                } else if (hasAccount == false) {
                    registerAccount(account)
                }

            }

        return hasAccount
    }

    // update local account
    private fun updateAccount(account: GoogleSignInAccount) {
        val db = Firebase.firestore
        UserManager.userId = account.email

        db.collection("users").whereEqualTo("id", UserManager.userId)
            .get()
            .addOnSuccessListener {

                if (it.documents.isNotEmpty()) {
                    val user = it.documents[0].toObject<User>()
                    if (user != null) {
                        UserManager.user = user
                        _navigateToExploreFragment.value = true
                    }
                }
            }

    }

    // register an account and update local account
    private fun registerAccount(account: GoogleSignInAccount) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "name" to account.displayName,
            "id" to account.email,
            "blockList" to listOf<String>(),
            "icon" to account.photoUrl,
            "completedEvents" to listOf<String>(),
            "likeChallenges" to listOf<String>(),
            "publicChallenges" to 0
        )

        db.collection("users")
            .add(user)
            .addOnSuccessListener {
                UserManager.userId = account.email

                // update local user(get user)
                db.collection("users").whereEqualTo("id", UserManager.userId)
                    .get()
                    .addOnSuccessListener {
                        it?.documents?.get(0)?.toObject<User>()?.let { UserManager.user = it }
                        _navigateToExploreFragment.value = true
                    }
            }
    }

    fun navigateToExploreFragmentCompleted() {
        _navigateToExploreFragment.value = false
    }


    // sign out
    private val _navigateToSignInFragment = MutableLiveData<Boolean>()
    val navigateToSignInFragment: LiveData<Boolean>
        get() = _navigateToSignInFragment

    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            _navigateToSignInFragment.value = true
        }
    }

    fun navigateToSignInFragmentCompleted() {
        _navigateToSignInFragment.value = false
    }
}