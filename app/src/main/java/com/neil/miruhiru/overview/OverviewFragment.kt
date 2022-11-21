package com.neil.miruhiru.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentOverviewBinding
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*


class OverviewFragment : Fragment() {

    private lateinit var binding: FragmentOverviewBinding
    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this).get(OverviewViewModel::class.java)
    }
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private lateinit var job: Job
    private var jobInitialized = false
    private lateinit var taskAdapter: OverViewAdapter
    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            Collections.swap(viewModel.customTaskList.value, startPosition, endPosition)
            recyclerView.itemAnimator = DefaultItemAnimator()

            // prevent from cancelling current move animation
            if (jobInitialized) { job.cancel() }
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

            Timber.i("duration ${recyclerView.itemAnimator?.moveDuration}")
            recyclerView.adapter?.itemCount?.let { itemCount ->
                scope.launch {
                    job = launch {
                        jobInitialized = true
                        delay(250)
                        recyclerView.itemAnimator = null
                        recyclerView.adapter?.notifyItemRangeChanged(0, itemCount)
                        this.cancel()
                    }
                }
            }

            setStartButtonToUpdate()
            return true
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        viewModel.customChallengeId = OverviewFragmentArgs.fromBundle(requireArguments()).customChallengeId


        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.customTaskRecycler)
        taskAdapter = OverViewAdapter(viewModel)  { task ->
            // send task and navigate to custom detail page
            setFragmentResult("fromOverview", bundleOf("task" to task))
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment(viewModel.customChallengeId))
        }
        binding.customTaskRecycler.adapter = taskAdapter

        viewModel.customTaskList.observe(viewLifecycleOwner, Observer {
            taskAdapter.submitList(it)
        })
        viewModel.loadCustomTasks()

        viewModel.resetStartButton.observe(viewLifecycleOwner, Observer { updateSuccess ->
            if (updateSuccess) {
                resetStartButton()
                viewModel.resetStartButtonCompleted()
            }
        })

        // observe editing status to determine button feacture
        viewModel.editingCompleted.observe(viewLifecycleOwner, Observer { editingCompleted ->
            if (editingCompleted) {
                binding.editOrUploadButton.text = getString(R.string.upload)
                binding.editOrUploadButton.setOnClickListener {
                    viewModel.uploadCustomChallengeBeVerified()
                }
            } else {
                binding.editOrUploadButton.text = getString(R.string.edit)
                binding.editOrUploadButton.setOnClickListener {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment(viewModel.customChallengeId))
                }
            }
        })

        // observe upload to change button
        viewModel.uploadToBeVerified.observe(viewLifecycleOwner, Observer { uploaded ->
            if (uploaded) {
                binding.editOrUploadButton.text = getString(R.string.uploaded)
                binding.editOrUploadButton.setOnClickListener(null)
            }
        })

        binding.startCustomButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(viewModel.customChallengeId))
        }

        binding.completeCustomButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomFragment())
        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun setStartButtonToUpdate() {
        binding.startCustomButton.text = getString(R.string.update)
        binding.startCustomButton.setOnClickListener(null)
        binding.startCustomButton.setOnClickListener {
            viewModel.updateCustomChallenge()
        }
    }

    private fun resetStartButton() {
        binding.startCustomButton.text = getString(R.string.challenge)
        binding.startCustomButton.setOnClickListener(null)
        binding.startCustomButton.setOnClickListener {
            // navigate to challenge detail
            this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(viewModel.customChallengeId))
        }
    }
}