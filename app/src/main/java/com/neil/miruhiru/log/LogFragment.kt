package com.neil.miruhiru.log

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
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.databinding.FragmentLogBinding
import com.neil.miruhiru.network.LoadingStatus
import timber.log.Timber
import java.text.SimpleDateFormat

class LogFragment : Fragment() {

    private lateinit var binding: FragmentLogBinding
    private val viewModel: LogViewModel by lazy {
        ViewModelProvider(this).get(LogViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogBinding.inflate(inflater, container, false)
        val eventId = LogFragmentArgs.fromBundle(requireArguments()).eventId

        // observe log
        val stageAdapter = LogStageAdapter(viewModel)
        binding.recyclerStage.adapter = stageAdapter
        viewModel.userInfoList.observe(viewLifecycleOwner, Observer {
            stageAdapter.submitList(viewModel.taskList.value)
            binding.logTitle.text = viewModel.challengeName
        })
        viewModel.loadCompletedChallenge(eventId)

        // observe timeSpent
        viewModel.timeSpent.observe(viewLifecycleOwner, Observer {
            binding.timeSpent.text = viewModel.convertSecondsToHours(it)
        })

        binding.logCompleteButton.setOnClickListener {
            if (this.findNavController().previousBackStackEntry?.destination?.id == R.id.profileFragment) {
                this.findNavController().navigateUp()
            } else {
                this.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
            }
        }

        // if challenge has been deleted, change the title to tell user about that
        viewModel.challengeDeleted.observe(viewLifecycleOwner, Observer { deleted ->
            if (deleted) {
                binding.logTitle.text = getString(R.string.challenge_deleted)
            }
        })

        // override back press
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (findNavController().previousBackStackEntry?.destination?.id == R.id.profileFragment) {
                        this@LogFragment.findNavController().navigateUp()
                    } else {
                        this@LogFragment.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
                    }
                }
            })

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            Timber.i("status ${status.name}")
            when (status) {
                LoadingStatus.LOADING -> {
                    MainActivity.getInstanceFromMainActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.VISIBLE
                }
                LoadingStatus.DONE -> {
                    MainActivity.getInstanceFromMainActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.GONE
                }
                LoadingStatus.ERROR -> {
                    MainActivity.getInstanceFromMainActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.GONE
                    Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show()
                }
            }
        })


        return binding.root
    }
}