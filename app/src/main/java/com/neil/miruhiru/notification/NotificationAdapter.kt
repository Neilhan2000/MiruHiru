package com.neil.miruhiru.notification

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.type.Date
import com.neil.miruhiru.R
import com.neil.miruhiru.chat.ChatMemberAdapter
import com.neil.miruhiru.data.Notification
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.ItemChatMemberBinding
import com.neil.miruhiru.databinding.ItemNotificationNotificationBinding
import java.text.SimpleDateFormat

class NotificationAdapter : ListAdapter<Notification, NotificationAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemNotificationNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun bind(item: Notification) {
            binding.notificationTitle.text = item.title
            binding.notificationContent.text = item.content
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            binding.sendTime.text = dateFormat.format(item.sendTime.toDate())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_notification, parent, false)
        return ViewHolder(binding = ItemNotificationNotificationBinding.bind(view))
    }

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Notification
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}