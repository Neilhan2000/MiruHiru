package com.neil.miruhiru.challengetype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentChallengeTypeBinding


class ChallengeTypeFragment : Fragment() {

    private lateinit var binding: FragmentChallengeTypeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeTypeBinding.inflate(inflater, container, false)
        setupScreen()
        return binding.root
    }

    private fun setupScreen() {
        binding.multiplePlayerButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalInviteFragment())
        }
    }

}