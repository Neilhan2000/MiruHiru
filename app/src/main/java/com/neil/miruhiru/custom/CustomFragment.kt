package com.neil.miruhiru.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentCustomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CustomFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCustomBinding.inflate(inflater, container, false)

        binding.viewPager.adapter = CustomPageAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = CustomTypeFilter.values()[position].value
        }.attach()
        if (this.findNavController().previousBackStackEntry?.destination?.id == R.id.overviewFragment) {
            binding.viewPager.setCurrentItem(1, false)
        }


        return binding.root
    }

}