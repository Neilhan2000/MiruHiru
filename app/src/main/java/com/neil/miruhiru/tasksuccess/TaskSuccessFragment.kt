package com.neil.miruhiru.tasksuccess

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.databinding.FragmentTaskSuccessBinding
import com.neil.miruhiru.network.LoadingStatus
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
        binding.chatRoomText.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalChatDialogFragment())
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

                requireActivity().onBackPressedDispatcher
                    .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            Toast.makeText(requireContext(), "按下完成挑戰才讓你離開OUo", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        })

        // observer event complete and navigate to challenge success or log fragment
        viewModel.navigateToLogFragment.observe(viewLifecycleOwner, Observer { completeEvent ->
            if (completeEvent && UserManager.isPersonal == false) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeSuccessFragment())
                viewModel.navigateToLogFragmentCompleted()

            } else if (completeEvent && UserManager.isPersonal == true) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalLogFragment(UserManager.user.completedEvents.last()))
                viewModel.navigateToLogFragmentCompleted()
            }
        })

        // button clickable set button
        viewModel.isButtonClickable.observe(viewLifecycleOwner, Observer { buttonClickable ->

            if (buttonClickable) {
                if (viewModel.currentStage < viewModel.stageNumber) {
                    Timber.i("isButtonClickable set 繼續挑戰")
                    binding.continueButton.text = "繼續挑戰"
                    binding.continueButton.backgroundTintList = (ColorStateList.valueOf(resources.getColor(R.color.deep_yellow)))
                    binding.continueButton.isEnabled = true
                    // remove snapshot listener
                    viewModel.removeDetectUsersProgress()
                } else {
                    Timber.i("isButtonClickable set 挑戰完成")
                    binding.continueButton.text = "挑戰完成"
                    binding.continueButton.backgroundTintList = (ColorStateList.valueOf(resources.getColor(R.color.deep_yellow)))
                    binding.continueButton.isEnabled = true
                    // remove snapshot listener
                    viewModel.removeDetectUsersProgress()
                }
            } else {
                binding.continueButton.text = "等待同伴挑戰中"
                binding.continueButton.backgroundTintList = (ColorStateList.valueOf(resources.getColor(R.color.grey)))
                binding.continueButton.isEnabled = false
            }

        })

        // observer if user is kicked out of challenge
        viewModel.isKicked.observe(viewLifecycleOwner, Observer { isKicked ->
            if (isKicked) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
                Toast.makeText(requireContext(), "你已被移出挑戰", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.detectUserKicked()

        // disable back press
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // do nothing
                }
            })

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            Timber.i("status ${status.name}")
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

        return binding.root
    }
}