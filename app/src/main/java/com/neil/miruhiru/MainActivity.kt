package com.neil.miruhiru

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.databinding.ActivityMainBinding
import com.tenclouds.fluidbottomnavigation.FluidBottomNavigationItem
import timber.log.Timber
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
        setupBottomNav()

        // login
        userLogin()

        // Timber
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
//        binding.fluidBottomNavigation.items =
//            listOf(
//                FluidBottomNavigationItem(
//                    getString(R.string.explore_fragment),
//                    ContextCompat.getDrawable(this, R.drawable.location_black_icon)),
//                FluidBottomNavigationItem(
//                    getString(R.string.custom_fragment),
//                    ContextCompat.getDrawable(this, R.drawable.custom_black_icon)),
//                FluidBottomNavigationItem(
//                    getString(R.string.community_fragment),
//                    ContextCompat.getDrawable(this, R.drawable.community_black_icon)),
//                FluidBottomNavigationItem(
//                    getString(R.string.profile_fragment),
//                    ContextCompat.getDrawable(this, R.drawable.profile_black_icon)))
//        binding.fluidBottomNavigation.accentColor = ContextCompat.getColor(this, R.color.deep_yellow)
//        binding.fluidBottomNavigation.backColor = ContextCompat.getColor(this, R.color.deep_yellow)
//        binding.fluidBottomNavigation.textColor = ContextCompat.getColor(this, R.color.deep_yellow)
//        binding.fluidBottomNavigation.iconColor = ContextCompat.getColor(this, R.color.deep_yellow)
//        binding.fluidBottomNavigation.iconSelectedColor = ContextCompat.getColor(this, R.color.deep_yellow)
    }

    private fun setupBottomNav() {
        val navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        binding.activityMainBottomNavigationView.setupWithNavController(navController)
        binding.activityMainBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.exploreFragment -> {
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalExploreFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.customFragment -> {
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalCustomFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.communityFragment -> {
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalCommunityFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.profileFragment -> {
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
                binding.activityMainBottomNavigationView.visibility = View.GONE
            } else {
                supportActionBar?.show()
                binding.activityMainBottomNavigationView.visibility = View.VISIBLE
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