package com.neil.miruhiru.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.FragmentCommunityBinding
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import com.zhpan.bannerview.constants.PageStyle
import com.zhpan.indicator.enums.IndicatorSlideMode

class CommunityFragment : Fragment() {

    private lateinit var viewPager: BannerViewPager<Challenge>
    private val viewModel: CommunityViewModel by lazy {
        ViewModelProvider(this).get(CommunityViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCommunityBinding.inflate(inflater, container, false)

        // Banner View Pager
        viewPager = binding.bannerView as BannerViewPager<Challenge>

        viewModel.bannerList.observe(viewLifecycleOwner, Observer{ challengeList ->
            binding.bannerView.apply {
                viewPager.adapter = BannerAdapter()
                registerLifecycleObserver(lifecycle)
                setIndicatorSlideMode(IndicatorSlideMode.WORM)
                setIndicatorSliderColor(
                    context.getColor(R.color.grey),
                    context.getColor(R.color.deep_yellow),
                )
                setIndicatorGravity(IndicatorGravity.END)
                setPageStyle(PageStyle.NORMAL)
                setOnPageClickListener { clickedView, position ->
                    val challengeId = viewPager.data[position].id
                    this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(challengeId))
                }
            }.create(challengeList)
        })

        val tagList = listOf(getString(R.string.popularity), getString(R.string.food), getString(R.string.history),
            getString(R.string.couple), getString(R.string.travel), getString(R.string.special))
        val tagAdapter = TagAdapter { tag ->
            viewModel.sortByTag(tag)
        }
        binding.tagRecycler.adapter = tagAdapter
        tagAdapter.submitList(tagList)

        val challengeAdapter = ChallengeAdapter { challengeId ->

        }
        binding.challengeRecycler.adapter = challengeAdapter
        viewModel.challengeList.observe(viewLifecycleOwner, Observer {
            challengeAdapter.submitList(it)
        })
        viewModel.loadChallengesByPopularity()

        return binding.root
    }

}