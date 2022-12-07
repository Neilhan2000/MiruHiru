package com.neil.miruhiru.challengetype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.ChallengeInfo
import com.neil.miruhiru.databinding.FragmentChallengeTypeBinding
import com.neil.miruhiru.network.LoadingStatus
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

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            if (status == LoadingStatus.LOADING) {
                MainActivity.getInstanceFromMainActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                binding.progressBar2.visibility = View.VISIBLE
            } else if (status == LoadingStatus.DONE) {
                MainActivity.getInstanceFromMainActivity().window.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                binding.progressBar2.visibility = View.GONE
            } else {
                MainActivity.getInstanceFromMainActivity().window.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getRandomString() : String {
        return java.util.UUID.randomUUID().toString()
    }

}