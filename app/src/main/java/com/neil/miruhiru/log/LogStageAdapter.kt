package com.neil.miruhiru.log

import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Photo
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemLogStageBinding

class LogStageAdapter(): ListAdapter<Task, LogStageAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemLogStageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            val photoAdapter = LogPhotoAdapter()
            val photoList = listOf(Photo(), Photo())
            binding.recyclerPhoto.adapter = photoAdapter
            photoAdapter.submitList(photoList)
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