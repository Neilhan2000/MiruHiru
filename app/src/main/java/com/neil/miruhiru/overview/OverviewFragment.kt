package com.neil.miruhiru.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentOverviewBinding
import kotlinx.coroutines.*
import okhttp3.internal.notifyAll
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
        taskAdapter = OverViewAdapter(viewModel)
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
        }
    }
}