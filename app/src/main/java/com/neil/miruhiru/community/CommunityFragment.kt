package com.neil.miruhiru.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.FragmentCommunityBinding
import com.zhpan.bannerview.BannerViewPager

class CommunityFragment : Fragment() {

    private lateinit var viewPager: BannerViewPager<Challenge>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCommunityBinding.inflate(inflater, container, false)

        viewPager = binding.bannerView as BannerViewPager<Challenge>
        binding.bannerView.apply {
            viewPager.adapter = BannerAdapter()
            setLifecycleRegistry(lifecycle)
        }.create(listOf(Challenge(), Challenge()))


        return binding.root
    }

}