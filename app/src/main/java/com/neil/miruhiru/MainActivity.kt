package com.neil.miruhiru

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Event
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.ActivityMainBinding
import timber.log.Timber
import timber.log.Timber.Forest.i
import timber.log.Timber.Forest.plant


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        private lateinit var instance: MainActivity

        fun getInstanceFromViewModel(): MainActivity? {
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // set activity instance
        instance = this

        // bottom navigation
        val navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        setupBottomNav()

        // login
        userLogin("user2")

        // Timber
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
    }

    private fun setupBottomNav() {
        binding.activityMainBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_explore -> {
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalExploreFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.fragment_custom -> {
//                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalChallengeDetailFragment())
//                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalCustomFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.fragment_community -> {
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalJoinFragment())
//                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalCommunityFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.fragment_profile -> {
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    private fun userLogin(userId: String) {
        UserManager.userId = userId
        UserManager.getUser()

        // check if has uncompleted event
        UserManager.hasCurrentEvent.observe(this, Observer { user ->
            if (user.currentEvent.isNotEmpty()) {
                val defaultBuilder = AlertDialog.Builder(this)
                    .setTitle("上次挑戰中斷")
                    .setMessage("將自動為你導向上次的紀錄")
                    .setPositiveButton("確定", object: DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalTaskFragment())
                        }
                    }).show()
                defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))
            }
        })
    }
//    fun cleanEvent(user: User) {
//        val db = Firebase.firestore
//
//        db.collection("events").whereEqualTo("id", user.currentEvent)
//            .get()
//            .addOnSuccessListener {
//                val event = it.documents[0].toObject<Event>()
//
//                event?.progress?.size?.let { progressMember ->
//                    if (progressMember > 1) {
//                        cleanEventMultiple()
//                    } else {
//                        cleanEventSingle()
//                    }
//                }
//            }
//
//    }

//    fun cleanEventSingle() {
//        val db = Firebase.firestore
//        var userDocumented = ""
//
//        db.collection("users").whereEqualTo("id", UserManager.userId)
//            .get()
//            .addOnSuccessListener {
//                userDocumented = it.documents[0].id
//
//                db.collection("users").document(userDocumented)
//                    .update("currentEvent", "")
//            }
//    }
//
//    fun cleanEventMultiple() {
//        val db = Firebase.firestore
//        var userDocumented = ""
//
//        db.collection("users").whereEqualTo("id", UserManager.userId)
//            .get()
//            .addOnSuccessListener {
//                userDocumented = it.documents[0].id
//
//                db.collection("users").document(userDocumented)
//                    .get()
//                    .addOnSuccessListener {
//                        val user = it.toObject<User>()
//                        Timber.i("user id ${user?.currentEvent}")
//
//                        db.collection("events").whereEqualTo("id", user?.currentEvent)
//                            .get()
//                            .addOnSuccessListener {
//                                val eventDocumented = it.documents[0].id
//
//                                // remove member and progress
//                                db.collection("events").document(eventDocumented)
//                                    .update("members", FieldValue.arrayRemove(UserManager.userId))
//                                    .addOnSuccessListener {
//
//                                        db.collection("events").document(eventDocumented)
//                                            .update("progress", FieldValue.arrayRemove(currentStage))
//                                            .addOnSuccessListener {
//
//                                                db.collection("users").document(userDocumented)
//                                                    .update("currentEvent", "")
//
//                                            }
//
//                                    }
//
//                            }
//
//                    }
//
//            }
//
//    }









}