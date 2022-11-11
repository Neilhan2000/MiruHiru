package com.neil.miruhiru.log

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentLogBinding
import com.neil.miruhiru.explore.ExploreViewModel

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


        val taskList = listOf(Task(), Task(), Task())
        val stageAdapter = LogStageAdapter()
        binding.recyclerStage.adapter = stageAdapter
        stageAdapter.submitList(taskList)




        return binding.root
    }
}