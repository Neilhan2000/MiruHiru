package com.neil.miruhiru.log

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
import com.neil.miruhiru.databinding.FragmentLogBinding
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
        })
        viewModel.loadCompletedChallenge(eventId)

        // observe timeSpent
        viewModel.timeSpent.observe(viewLifecycleOwner, Observer {
            binding.timeSpent.text = viewModel.convertSecondsToHours(it)
        })

        binding.logCompleteButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
        }


        return binding.root
    }
}