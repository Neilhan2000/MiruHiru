package com.neil.miruhiru

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar
        UserManager.userLiveData.observe(this, Observer {
            setupToolbar()
        })

        UserManager.customCurrentStage = null
        UserManager.customTotalStage = null

        // set activity instance
        instance = this

        // bottom navigation
        val navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        setupBottomNav()

        // login
        userLogin()

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
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalCustomFragment())
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

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)

        findNavController(R.id.myNavHostFragment).addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.exploreFragment -> CurrentFragmentType.EXPLORE
                R.id.customFragment -> CurrentFragmentType.CUSTOM
                R.id.communityFragment -> CurrentFragmentType.COMMUNITY
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                else -> CurrentFragmentType.OTHER
            }
        }
        viewModel.currentFragmentType.observe(this, Observer { fragmentType ->

            if (fragmentType.value == getString(R.string.other)) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
            }

            if (fragmentType.value == getString(R.string.explore_fragment)) {
                binding.userIconExplore.visibility = View.VISIBLE
                Glide.with(binding.userIconExplore.context).load(UserManager.user.icon).circleCrop().apply(
                    RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
                ).into(binding.userIconExplore)
                binding.toolbarTitle.text = UserManager.user.name
            } else {
                binding.toolbarTitle.text = fragmentType.value
                binding.userIconExplore.visibility = View.GONE
            }
        })
    }

    private fun userLogin() {
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