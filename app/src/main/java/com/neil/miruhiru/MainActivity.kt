package com.neil.miruhiru

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.neil.miruhiru.task.TaskViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import timber.log.Timber.Forest.i
import timber.log.Timber.Forest.plant


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    companion object {
        private lateinit var instance: MainActivity

        fun getInstanceFromMainActivity(): MainActivity {
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
        userLogin("user1")

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
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalLoginFragment())
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
        UserManager.hasCurrentEvent.observe(this, Observer { event ->
            if (event.progress.size == 1) {
                val defaultBuilder = AlertDialog.Builder(this)
                    .setTitle("上次挑戰中斷")
                    .setMessage("要繼續上次的進度嗎")
                    .setPositiveButton("確定", object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalTaskFragment())
                        }
                    })
                    .setNegativeButton("取消", object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            // do nothing
                        }
                    })
                    .setNeutralButton("清除紀錄", object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            viewModel.cleanEventSingle()
                        }
                    })
                    .show()
                defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))
                defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))
                defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))
            } else {
                val defaultBuilder = AlertDialog.Builder(this)
                    .setTitle("上次挑戰中斷")
                    .setMessage("要繼續上次的進度嗎")
                    .setPositiveButton("確定", object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalTaskFragment())
                        }
                    })
                    .setNeutralButton("清除紀錄", object : DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            viewModel.cleanEventMultiple()
                        }
                    })
                    .show()
                defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))
                defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setTextColor(ContextCompat.getColor(this, R.color.deep_yellow))
            }

        })
    }










}