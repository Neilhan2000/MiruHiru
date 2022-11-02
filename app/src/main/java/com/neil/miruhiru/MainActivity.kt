package com.neil.miruhiru

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.neil.miruhiru.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // bottom navigation
        val navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        setupBottomNav()
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
                    findNavController(R.id.myNavHostFragment).navigate(NavGraphDirections.actionGlobalCommunityFragment())
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
}