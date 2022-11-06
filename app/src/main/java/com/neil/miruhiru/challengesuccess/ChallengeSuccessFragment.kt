package com.neil.miruhiru.challengesuccess

import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentChallengeSuccessBinding

class ChallengeSuccessFragment : Fragment() {

    private lateinit var binding: FragmentChallengeSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeSuccessBinding.inflate(inflater, container, false)
        setupScreen()







        return binding.root
    }

    private fun setupScreen() {
        binding.postButton.setOnClickListener {
            this.findNavController()
        }
        binding.cancelButton

    }

}