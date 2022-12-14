package com.neil.miruhiru.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Log
import com.neil.miruhiru.databinding.ItemLogPhotoBinding
import timber.log.Timber

class LogLogAdapter(viewModel: LogViewModel, adapterPosition: Int) : ListAdapter<Log, LogLogAdapter.ViewHolder>(DiffCallBack()) {

    private val viewModel = viewModel
    private val stage = adapterPosition + 1

    inner class ViewHolder(private val binding: ItemLogPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Log) {

//            if (stage == item.stage) {
                // photo
                if (item.photo.isNotEmpty()) {
                    Glide.with(binding.taskPhoto.context).load(item.photo).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
                    ).into(binding.taskPhoto)
                    Timber.i("photo not empty ${item.photo}")
                } else {
                    binding.taskPhoto.layoutParams.height = 0
                    binding.taskPhotoBorder.layoutParams.height = 0
                }

                // user
                viewModel.userInfoList.value?.forEach {
                    Timber.i("userlist = ${viewModel.userInfoList.value}")
                    if (it.id == item.senderId) {
                        binding.userNameLog.text = it.name
                        Glide.with(binding.userIconLog.context).load(it.icon).circleCrop().apply(
                            RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
                        ).into(binding.userIconLog)
                    }
                }
                binding.userText.text = item.text
//            } else {
//                itemView.layoutParams.height = 0
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogLogAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_photo, parent, false)
        return ViewHolder(binding = ItemLogPhotoBinding.bind(view))
    }

    override fun onBindViewHolder(holder: LogLogAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Log
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Log>() {
        override fun areItemsTheSame(oldItem: Log, newItem: Log): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Log, newItem: Log): Boolean {
            return oldItem == newItem
        }
    }
}