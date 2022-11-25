package com.neil.miruhiru.verify

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
import com.neil.miruhiru.databinding.FragmentVerifyBinding
import com.neil.miruhiru.databinding.FragmentVerifyDetailBinding
import com.neil.miruhiru.verifydetail.VerifyDetailViewModel


class VerifyFragment : Fragment() {

    private lateinit var binding: FragmentVerifyBinding
    private val viewModel: VerifyViewModel by lazy {
        ViewModelProvider(this).get(VerifyViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerifyBinding.inflate(inflater, container, false)

        val challengeAdapter = VerifyChallengeAdapter { challengeId ->
            this.findNavController().navigate(NavGraphDirections.actionGlobalVerifyDetailFragment(challengeId))
        }
        binding.unverifiedRecycler.adapter = challengeAdapter
        viewModel.unverifiedChallenges.observe(viewLifecycleOwner, Observer {
            challengeAdapter.submitList(it)
        })
        viewModel.loadUnverifiedChallenges()











        return binding.root
    }

}