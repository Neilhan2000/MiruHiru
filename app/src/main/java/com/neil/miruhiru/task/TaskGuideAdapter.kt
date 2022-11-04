package com.neil.miruhiru.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemTaskGuideBinding


class TaskGuideAdapter: ListAdapter<Task, TaskGuideAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemTaskGuideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            binding.guideText.text = item.guide
            if (adapterPosition == 0 || adapterPosition == 2) {
                itemView.visibility = View.INVISIBLE
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskGuideAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_guide, parent, false)
        return ViewHolder(binding = ItemTaskGuideBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TaskGuideAdapter.ViewHolder, position: Int) {
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