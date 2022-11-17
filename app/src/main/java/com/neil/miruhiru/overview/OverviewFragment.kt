package com.neil.miruhiru.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentOverviewBinding
import okhttp3.internal.notifyAll
import timber.log.Timber
import java.util.*


class OverviewFragment : Fragment() {

    private lateinit var binding: FragmentOverviewBinding
    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this).get(OverviewViewModel::class.java)
    }
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
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            if (endPosition == 0) {
                recyclerView.adapter?.notifyDataSetChanged()
            } else {
                recyclerView.adapter?.notifyItemChanged(startPosition)
                recyclerView.adapter?.notifyItemChanged(endPosition)
            }


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





        return binding.root
    }

}