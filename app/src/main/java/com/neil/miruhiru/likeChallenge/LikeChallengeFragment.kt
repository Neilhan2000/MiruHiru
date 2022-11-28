package com.neil.miruhiru.likeChallenge

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentLikeChallengeBinding
import com.neil.miruhiru.invite.InviteFragmentViewModel

class LikeChallengeFragment : Fragment() {

    private lateinit var binding: FragmentLikeChallengeBinding
    private val viewModel: LikeChallengeViewModel by lazy {
        ViewModelProvider(this).get(LikeChallengeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLikeChallengeBinding.inflate(inflater, container, false)

        val challengeAdapter = LikeChallengeAdapter { challengeId ->
            this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(challengeId))
        }
        binding.likeChallengeRecycler.adapter = challengeAdapter
        viewModel.likeChallengeList.observe(viewLifecycleOwner, Observer {
            challengeAdapter.submitList(it)
        })
        viewModel.loadLikeChallenges()


        return binding.root
    }

}