package com.neil.miruhiru.challengetype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.ChallengeInfo
import com.neil.miruhiru.databinding.FragmentChallengeTypeBinding
import timber.log.Timber


class ChallengeTypeFragment : Fragment() {

    private lateinit var binding: FragmentChallengeTypeBinding
    private lateinit var challenge: ChallengeInfo
    private lateinit var eventId: String
    private val viewModel: ChallengeTypeViewModel by lazy {
        ViewModelProvider(this).get(ChallengeTypeViewModel::class.java)
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
        eventId = getRandomString()
        Timber.i("$eventId")
        viewModel.navigateToTaskFragment.observe(viewLifecycleOwner, Observer { postEventSuccess ->
            if (postEventSuccess == "single") {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                viewModel.navigateToTaskFragmentCompleted()
            } else if (postEventSuccess == "multiple") {
                this.findNavController().navigate(NavGraphDirections.actionGlobalInviteFragment(eventId))
                viewModel.navigateToTaskFragmentCompleted()
            }
        })

        binding.singlePlayerButton.setOnClickListener {
            viewModel.postEvent(eventId, challenge, "single")
        }
        binding.multiplePlayerButton.setOnClickListener {
            Timber.i("$eventId")
            viewModel.postEvent(eventId, challenge, "multiple")
        }

        // enable back press, it has problem that its navigateUp is no working
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this@ChallengeTypeFragment.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
                }
            })
    }

    private fun getRandomString() : String {
        return java.util.UUID.randomUUID().toString()
    }

}