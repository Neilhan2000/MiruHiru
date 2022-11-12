package com.neil.miruhiru.tasksuccess

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
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

        // current event set button
        viewModel.getCurrentStage.observe(viewLifecycleOwner, Observer { currentStage ->
            Timber.i("current${currentStage}, stage${viewModel.stageNumber}")
            if (currentStage < viewModel.stageNumber) {
                binding.continueButton.setOnClickListener {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                    viewModel.navigateToTaskFragmentCompleted()
                }
            } else {
                binding.continueButton.text = "挑戰完成"

                binding.continueButton.setOnClickListener(null)
                binding.continueButton.setOnClickListener {
                    viewModel.completeEvent()
                    viewModel.completeChallenge()
                }
            }
        })

        // observer event complete and navigate to log fragment
        viewModel.navigateToLogFragment.observe(viewLifecycleOwner, Observer { completeEvent ->
            if (completeEvent) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeSuccessFragment())
                viewModel.navigateToLogFragmentCompleted()
            }
        })

        // button clickable set button
        viewModel.isButtonClickable.observe(viewLifecycleOwner, Observer { buttonClickable ->

            if (binding.continueButton.text != "挑戰完成") {
                if (buttonClickable) {
                    Timber.i("isButtonClickable set 繼續挑戰")
                    binding.continueButton.text = "繼續挑戰"
                    binding.continueButton.setBackgroundResource(R.drawable.button_border)
                    binding.continueButton.isEnabled = true
                    // remove snapshot listener
                    viewModel.removeDetectUsersProgress()
                } else {
                    binding.continueButton.text = "等待同伴挑戰中"
                    binding.continueButton.setBackgroundResource(R.drawable.button_disable_border)
                    binding.continueButton.isEnabled = false
                }
            }
        })

        // disable back press
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // do nothing
                }
            })






        return binding.root
    }
}