package com.neil.miruhiru.custom

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.neil.miruhiru.custom.item.MyCustomFragment
import com.neil.miruhiru.custom.item.StartCustomFragment

class CustomPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return CustomTypeFilter.values().size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> StartCustomFragment()
            else -> MyCustomFragment()
        }
    }
}