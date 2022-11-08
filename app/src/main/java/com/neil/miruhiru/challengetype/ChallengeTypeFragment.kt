package com.neil.miruhiru.challengetype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.challengedetail.ChallengeDetailFragmentArgs
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.FragmentChallengeTypeBinding


class ChallengeTypeFragment : Fragment() {

    private lateinit var binding: FragmentChallengeTypeBinding
    private lateinit var challenge: Challenge
    private lateinit var eventId: String
    private val viewModel: TypeAndInviteViewModel by lazy {
        ViewModelProvider(this).get(TypeAndInviteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeTypeBinding.inflate(inflater, container, false)
        challenge = ChallengeTypeFragmentArgs.fromBundle(requireArguments()).challenge
        setupScreen()
        return binding.root
    }

    private fun setupScreen() {
        eventId = getRandomString(20)
        binding.multiplePlayerButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalInviteFragment(challenge))
        }
        viewModel.navigateToTaskFragment.observe(viewLifecycleOwner, Observer { postEventSuccess ->
            if (postEventSuccess == true) {
                // update user current event data
                viewModel.updateUserCurrentEvent(eventId)

                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                viewModel.navigateToTaskFragmentCompleted()
            }
        })
        binding.singlePlayerButton.setOnClickListener {
            viewModel.postEvent(eventId, challenge)
        }
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}