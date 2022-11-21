package com.neil.miruhiru.invite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.ItemInviteUserBinding


class UserAdapter : ListAdapter<User, UserAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemInviteUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User) {
            Glide.with(binding.userIconInvite.context).load(item.icon).circleCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
            ).into(binding.userIconInvite)
            binding.userNameInvite.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invite_user, parent, false)
        return ViewHolder(binding = ItemInviteUserBinding.bind(view))
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
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