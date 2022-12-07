package com.neil.miruhiru.chat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.community.TagAdapter
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.ItemChatMemberBinding
import com.neil.miruhiru.databinding.ItemCommunityTagBinding

class ChatMemberAdapter : ListAdapter<User, ChatMemberAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemChatMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User) {
            Glide.with(binding.memberIcon.context).load(item.icon).circleCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
            ).into(binding.memberIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMemberAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_member, parent, false)
        return ViewHolder(binding = ItemChatMemberBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ChatMemberAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as User
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}