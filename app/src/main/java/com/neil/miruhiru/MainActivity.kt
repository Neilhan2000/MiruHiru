package com.neil.miruhiru

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Color.argb
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
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
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.neil.miruhiru.data.Notification
import com.neil.miruhiru.databinding.ActivityMainBinding
import com.neil.miruhiru.ext.getVmFactory
import com.neil.miruhiru.ext.glideImageCircle
import com.neil.miruhiru.util.Util.showDialog2Options
import com.neil.miruhiru.util.Util.showDialog3Options
import com.neil.miruhiru.util.Util.showToast
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.Circle
import timber.log.Timber
import timber.log.Timber.Forest.plant

/**
 * Created by Neil Tsai on Dec 2022.
 */
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var badge: BadgeDrawable
    private lateinit var navController: NavController

    private val viewModel by viewModels<MainViewModel> { getVmFactory() }

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

        // toolbar
        UserManager.userLiveData.observe(this, Observer {
            setupToolbar()
            viewModel.detectNotifications()
        })

        // bottom navigation
        setupBottomNav()

        // firebase error
        viewModel.error.observe(this, Observer {
            if (it != null) {
                showToast(it)
            }
        })

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
                    if (navController.currentDestination?.id != R.id.exploreFragment) {
                        navController.navigate(NavGraphDirections.actionGlobalExploreFragment())
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.customFragment -> {
                    if (navController.currentDestination?.id != R.id.customFragment) {
                        navController.navigate(NavGraphDirections.actionGlobalCustomFragment())
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.communityFragment -> {
                    if (navController.currentDestination?.id != R.id.communityFragment) {
                        navController.navigate(NavGraphDirections.actionGlobalCommunityFragment())
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.profileFragment -> {
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(NavGraphDirections.actionGlobalProfileFragment())
                    }
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        badge = binding.activityMainBottomNavigationView.getOrCreateBadge(R.id.profileFragment)
        badge.isVisible = false
        badge.backgroundColor = getColor(R.color.red)

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
                R.id.inviteFragment -> CurrentFragmentType.INVITE
                R.id.challengeTypeFragment -> CurrentFragmentType.CHALLENGETYPE
                else -> CurrentFragmentType.OTHER
            }
        }

        // observe fragment type
        viewModel.currentFragmentType.observe(this, Observer { fragmentType ->
            // set bottom navigation and back icon
            if (fragmentType.value == getString(R.string.explore_fragment) ||
                    fragmentType.value == getString(R.string.custom_fragment) ||
                    fragmentType.value == getString(R.string.community_fragment) ||
                    fragmentType.value == getString(R.string.profile_fragment)
            ) {
                binding.activityMainBottomNavigationView.visibility = View.VISIBLE
                binding.backIcon.visibility = View.GONE
            } else {
                binding.activityMainBottomNavigationView.visibility = View.GONE
                binding.backIcon.visibility = View.VISIBLE
            }

            // overview fragment back icon press
            if (fragmentType.value == getString(R.string.overview_fragment)) {
                binding.backIcon.setOnClickListener {
                    navController.navigate(NavGraphDirections.actionGlobalCustomFragment(1))
                }
            } else {
                binding.backIcon.setOnClickListener {
                    navController.navigateUp()
                }
            }

            // set toolbar
            if (fragmentType.value == getString(R.string.other) || fragmentType.value == getString(R.string.challenge_detail_fragment)) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
                when (fragmentType.value) {
                    getString(R.string.explore_fragment) -> {
                        binding.userIconExplore.visibility = View.VISIBLE
                        binding.userIconExplore.glideImageCircle(
                            UserManager.user.icon ?: "",
                            R.drawable.ic_user_no_photo
                        )
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
                showDialog3Options(
                    getString(R.string.challenge_interrupted),
                    getString(R.string.continue_last_challenge),
                    positiveFun = { navController.navigate(NavGraphDirections.actionGlobalTaskFragment()) },
                    neutralFun = { viewModel.cleanEventSingle() }
                )
            } else {
                showDialog2Options(
                    getString(R.string.challenge_interrupted),
                    getString(R.string.continue_last_challenge),
                    positiveFun = { navController.navigate(NavGraphDirections.actionGlobalTaskFragment()) },
                    negativeFun = { viewModel.cleanEventMultiple() }
                )
            }
        })
    }

    /**
     * Below two functions are designed for profile fragment to control badge display.
     */
    fun cleanBadge() {
        badge.isVisible = false
    }
    fun isBadgeVisible(): Boolean {
        return badge.isVisible
    }


}