package com.neil.miruhiru.join

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
import com.neil.miruhiru.challengetype.ChallengeTypeViewModel
import com.neil.miruhiru.databinding.FragmentJoinBinding

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding
    private lateinit var eventId: String
    private val viewModel: JoinEventViewModel by lazy {
        ViewModelProvider(this).get(JoinEventViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinBinding.inflate(inflater, container, false)
        binding.openCameraButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalScanFragment())
        }

        binding.joinChallengeButton.setOnClickListener {
            eventId = binding.editTextChallengeKey.text.toString()
            viewModel.addScanUserToEvent(eventId, "multiple")
        }

        viewModel.navigateToTaskFragment.observe(viewLifecycleOwner, Observer { addToEvent ->
            if (addToEvent == "multiple") {
                this.findNavController().navigate(NavGraphDirections.actionGlobalInviteFragment(eventId))
                viewModel.navigateToTaskFragmentCompleted()
            }
        })

        return binding.root
    }

}