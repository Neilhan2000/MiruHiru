package com.neil.miruhiru.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemTaskTaskBinding

class TaskAdapter: ListAdapter<Task, TaskAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemTaskTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            Glide.with(binding.challengeImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
            ).into(binding.challengeImage)
            binding.challengeTitle.text = item.name
            binding.challengeStage.text = item.stage.toString()


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task_task, parent, false)
        return ViewHolder(binding = ItemTaskTaskBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TaskAdapter.ViewHolder, position: Int) {
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