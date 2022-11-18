package com.neil.miruhiru.overview

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.ItemOverviewTaskBinding
import com.neil.miruhiru.invite.UserAdapter
import kotlin.math.roundToInt

class OverViewAdapter(private val viewModel: OverviewViewModel) : ListAdapter<Task, OverViewAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemOverviewTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            Glide.with(binding.overviewTaskImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
            ).into(binding.overviewTaskImage)
            binding.taskNameText.text = item.name
            setDistanceText(item)
            viewModel.resetTasksStages(adapterPosition)
        }

        private fun setDistanceText(item: Task) {
            if (adapterPosition == 0) {
                binding.taskDistanceText.text = "起始點"
            } else {

                val startPoint = viewModel.customTaskList.value?.get(0)
                val destination = item.location
                val result =  floatArrayOf(0.0F)
                startPoint?.location?.let {
                    Location.distanceBetween(
                        it.latitude, it.longitude,
                        destination.latitude, destination.longitude, result
                    )
                }
                binding.taskDistanceText.text = "距離起點 ${result[0].roundToInt()} Ms"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_overview_task, parent, false)
        return ViewHolder(binding = ItemOverviewTaskBinding.bind(view))
    }

    override fun onBindViewHolder(holder: OverViewAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Task
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}