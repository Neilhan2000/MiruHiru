package com.neil.miruhiru.tasksuccess

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
            if (currentStage <= viewModel.stageNumber) {
                binding.continueButton.setOnClickListener {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                }
            } else {
                binding.continueButton.text = "結束挑戰"
                Timber.i("getcurrentstage set 結束挑戰")
                binding.continueButton.setOnClickListener(null)
                binding.continueButton.setOnClickListener {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeSuccessFragment())
                }
            }
        })

        // button clickable set button
        viewModel.isButtonClickable.observe(viewLifecycleOwner, Observer { buttonClickable ->

            if (binding.continueButton.text != "結束挑戰") {
                if (buttonClickable) {
                    Timber.i("isButtonClickable set 繼續挑戰")
                    binding.continueButton.text = "繼續挑戰"
                    binding.continueButton.setBackgroundResource(R.drawable.button_border)
                    binding.continueButton.isEnabled = true
                    // remove snapshot listener
                } else {
                    binding.continueButton.text = "等待同伴挑戰中"
                    binding.continueButton.setBackgroundResource(R.drawable.button_disable_border)
                    binding.continueButton.isEnabled = false
                }
            }
//            else if (binding.continueButton.text == "等待同伴挑戰中"){
//                binding.continueButton.setOnClickListener(null)
//                binding.continueButton.setOnClickListener {
//                    this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeSuccessFragment())
//                }
//            }
        })







        return binding.root
    }
}