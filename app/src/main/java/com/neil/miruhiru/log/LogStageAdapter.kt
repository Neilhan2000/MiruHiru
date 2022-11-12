package com.neil.miruhiru.log

import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Log
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemLogStageBinding
import timber.log.Timber

class LogStageAdapter(viewModel: LogViewModel) : ListAdapter<Task, LogStageAdapter.ViewHolder>(DiffCallBack()) {

    private val viewModel = viewModel

    inner class ViewHolder(private val binding: ItemLogStageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            binding.stageName .text = item.name
            binding.stageNumber.text = when (item.stage) {
                1 -> "第一關"
                2 -> "第二關"
                3 -> "第三關"
                4 -> "第四關"
                else -> "第五關"
            }

            val logAdapter = LogLogAdapter(viewModel, adapterPosition)
            binding.recyclerPhoto.adapter = logAdapter
            Timber.i("loglist ${viewModel.logList.value}")
            logAdapter.submitList(viewModel.logList.value)
            if (adapterPosition == logAdapter.itemCount) {
                binding.greyLine7.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogStageAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_stage, parent, false)
        return ViewHolder(binding = ItemLogStageBinding.bind(view))
    }

    override fun onBindViewHolder(holder: LogStageAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Task
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}