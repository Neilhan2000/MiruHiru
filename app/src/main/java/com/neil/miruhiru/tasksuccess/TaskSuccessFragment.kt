package com.neil.miruhiru.tasksuccess

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.databinding.FragmentTaskSuccessBinding
import timber.log.Timber

class TaskSuccessFragment : Fragment() {

    private lateinit var binding: FragmentTaskSuccessBinding
    private lateinit var viewModel: TaskSAndLogDViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskSuccessBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(TaskSAndLogDViewModel::class.java)
        binding.logButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalLogDialogFragment())
        }
        binding.continueButton.setOnClickListener {
            Timber.i("current${viewModel.currentStage}, stage${viewModel.stageNumber}")
            if (viewModel.currentStage < viewModel.stageNumber) {
                viewModel.updateProgress()
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
            } else {
                viewModel.completeEvent()
                viewModel.completeChallenge()
                this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeSuccessFragment())
            }
        }







        return binding.root
    }
}