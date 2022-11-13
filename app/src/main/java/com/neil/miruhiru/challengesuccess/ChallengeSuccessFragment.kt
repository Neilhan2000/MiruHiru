package com.neil.miruhiru.challengesuccess

import android.graphics.Color
import android.graphics.ColorFilter
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
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.challengedetail.ChallengeDetailViewModel
import com.neil.miruhiru.challengetype.ChallengeTypeViewModel
import com.neil.miruhiru.databinding.FragmentChallengeSuccessBinding
import timber.log.Timber

class ChallengeSuccessFragment : Fragment() {

    private lateinit var binding: FragmentChallengeSuccessBinding
    private val viewModel: ChallengeSuccessViewModel by lazy {
        ViewModelProvider(this).get(ChallengeSuccessViewModel::class.java)
    }

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
            Timber.i("post button click")
            viewModel.postComment(binding.successRatingBar.rating, binding.editTextComment.text.toString())
        }
        viewModel.navigateToLogFragment.observe(viewLifecycleOwner, Observer { postSuccess ->
            if (postSuccess) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalLogFragment())
                viewModel.navigateToLogFragmentCompleted()
            }
        })

        binding.cancelButton.setOnClickListener {
            UserManager.userChallengeDocumentId = null
            this.findNavController().navigate(NavGraphDirections.actionGlobalLogFragment())
        }

    }

}