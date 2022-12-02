package com.neil.miruhiru

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
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
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Notification
import com.neil.miruhiru.databinding.ActivityMainBinding
import timber.log.Timber
import timber.log.Timber.Forest.plant


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var badge: BadgeDrawable
    private lateinit var navController: NavController
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
            viewModel.detectNotifications()
        })

        UserManager.customCurrentStage = null
        UserManager.customTotalStage = null

        // set activity instance
        instance = this

        // bottom navigation
        setupBottomNav()

        // login
        userLogin()

        // Timber
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
    }
    private fun setupBottomNav() {
        navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        binding.activityMainBottomNavigationView.setupWithNavController(navController)
        binding.activityMainBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.exploreFragment -> {
                    navController.navigate(NavGraphDirections.actionGlobalExploreFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.customFragment -> {
                    navController.navigate(NavGraphDirections.actionGlobalCustomFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.communityFragment -> {
                    navController.navigate(NavGraphDirections.actionGlobalCommunityFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.profileFragment -> {
                    navController.navigate(NavGraphDirections.actionGlobalProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        badge = binding.activityMainBottomNavigationView.getOrCreateBadge(R.id.profileFragment)
        badge.isVisible = false
        badge.backgroundColor = ContextCompat.getColor(this, R.color.red)
        viewModel.notificationList.observe(this, Observer {
            Timber.i("notifications $it manager notifications ${UserManager.readNotifications}")
            val unReadNotifications = it.size - (UserManager.readNotifications ?: 0)
            if (unReadNotifications != 0) {
                badge.number = unReadNotifications
                badge.isVisible = true
            } else {
                badge.isVisible = false
            }

        })
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.backIcon.setOnClickListener {
            navController.navigateUp()
        }

        navController.addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.exploreFragment -> CurrentFragmentType.EXPLORE
                R.id.customFragment -> CurrentFragmentType.CUSTOM
                R.id.communityFragment -> CurrentFragmentType.COMMUNITY
                R.id.profileFragment -> CurrentFragmentType.PROFILE
                R.id.challengeDetailFragment -> CurrentFragmentType.CHALLENGEDETAIL
                R.id.customChallengeFragment -> CurrentFragmentType.CUSTOMDETAIL
                R.id.overviewFragment -> CurrentFragmentType.OVERVIEW
                R.id.likeChallengeFragment -> CurrentFragmentType.LIKE
                R.id.joinFragment -> CurrentFragmentType.JOIN
                R.id.notificationFragment -> CurrentFragmentType.NOTIFICATION
                else -> CurrentFragmentType.OTHER
            }
        }
        viewModel.currentFragmentType.observe(this, Observer { fragmentType ->
            // bottom navigation and back icon
            if (fragmentType.value == getString(R.string.explore_fragment) ||
                    fragmentType.value == getString(R.string.custom_fragment) ||
                    fragmentType.value == getString(R.string.community_fragment) ||
                    fragmentType.value == getString(R.string.profile_fragment)) {
                binding.activityMainBottomNavigationView.visibility = View.VISIBLE
                binding.backIcon.visibility = View.GONE
            } else {
                binding.activityMainBottomNavigationView.visibility = View.GONE
                binding.backIcon.visibility = View.VISIBLE
            }

            // toolbar
            if (fragmentType.value == getString(R.string.other) || fragmentType.value == getString(R.string.challenge_detail_fragment)) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
                when (fragmentType.value) {
                    getString(R.string.explore_fragment) -> {
                        binding.userIconExplore.visibility = View.VISIBLE
                        Glide.with(binding.userIconExplore.context).load(UserManager.user.icon)
                            .circleCrop().apply(
                            RequestOptions().placeholder(R.drawable.ic_user_no_photo)
                                .error(R.drawable.ic_user_no_photo)
                        ).into(binding.userIconExplore)
                        binding.toolbarTitle.text = UserManager.user.name
                    }
                    else -> {
                        binding.toolbarTitle.text = fragmentType.value
                        binding.userIconExplore.visibility = View.GONE
                    }
                }
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

    // for Profile Fragment to use
    fun cleanBadge() {
        badge.isVisible = false
    }

    fun isBadgeVisible(): Boolean {
        return badge.isVisible
    }


}