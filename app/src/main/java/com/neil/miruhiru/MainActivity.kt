package com.neil.miruhiru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.neil.miruhiru.databinding.ActivityMainBinding
import timber.log.Timber
import timber.log.Timber.Forest.i
import timber.log.Timber.Forest.plant


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalChallengeDetailFragment())
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
//        UserManager.user.observe(this, Observer {
//            Timber.i("$it")
//        })
    }
}