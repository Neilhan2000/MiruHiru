package com.neil.miruhiru.task

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearSnapHelper
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentTaskBinding

class TaskFragment : Fragment() {

    private val viewModel: TaskViewModel by lazy {
        ViewModelProvider(this).get(TaskViewModel::class.java)
    }
    // animation
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_form_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_to_bottom_anim) }
    private var clicked = false

    private lateinit var binding: FragmentTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)

        // floating action button
        binding.fabAdd.setOnClickListener {
            onAddButtonClick()
        }
        binding.fabChat.setOnClickListener {
            Toast.makeText(requireContext(), "chat clicked", Toast.LENGTH_SHORT).show()
        }
        binding.fabAndroid.setOnClickListener {
            binding.guideTextRecycler.scrollToPosition(1)
            binding.guideTextRecycler.visibility = View.VISIBLE
        }

        // task and guide adapter
        val taskAdapter = TaskAdapter()
        val guideAdapter = TaskGuideAdapter()
        binding.TaskRecycler.adapter = taskAdapter
        binding.guideTextRecycler.adapter = guideAdapter
        LinearSnapHelper().apply {
            attachToRecyclerView(binding.guideTextRecycler)
        }

        // observe taskList and setup screen
        viewModel.taskList.observe(viewLifecycleOwner, Observer {
            taskAdapter.submitList(it)
            binding.progressBar.width = (binding.progressBarBorder.width - 16) / 5 * (viewModel.event.value?.progress?.minOrNull() ?: 0)
            binding.progressText.text = "${it[0].stage} / 5"

            val guildTextList = listOf<Task>(Task(), it[0], Task())
            guideAdapter.submitList(guildTextList)
            binding.guideTextRecycler.scrollToPosition(1)
        })
//        val fakeTaskList = listOf<Task>(
//            Task(),
//            Task(),
//            Task()
//        )
//        adapter.submitList(fakeTaskList)
//        var totalScrollX = 0
//        binding.recyclerTask.setOnScrollChangeListener { _, _, _, offsetX, _ ->
//            totalScrollX += offsetX
//            Log.i("neil", "scroll x = $totalScrollX")
//        }
//        binding.recyclerTask.scrollToPosition(1)

        return binding.root
    }

    private fun onAddButtonClick() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.fabChat.visibility = View.VISIBLE
            binding.fabAndroid.visibility = View.VISIBLE
        } else {
            binding.fabChat.visibility = View.INVISIBLE
            binding.fabAndroid.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.fabChat.startAnimation(fromBottom)
            binding.fabAndroid.startAnimation(fromBottom)
            binding.fabAdd.startAnimation(rotateOpen)
        } else {
            binding.fabChat.startAnimation(toBottom)
            binding.fabAndroid.startAnimation(toBottom)
            binding.fabAdd.startAnimation(rotateClose)
        }
    }
}