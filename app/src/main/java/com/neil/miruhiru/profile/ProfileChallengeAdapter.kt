package com.neil.miruhiru.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.User
import com.neil.miruhiru.databinding.ItemInviteUserBinding
import com.neil.miruhiru.databinding.ItemProfileCompletedChallengeBinding


class ProfileChallengeAdapter : ListAdapter<Challenge, ProfileChallengeAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemProfileCompletedChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Challenge) {
            Glide.with(binding.completedChallengeImage.context).load(item.image).circleCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_challenge_success).error(R.drawable.ic_challenge_success)
            ).into(binding.completedChallengeImage)
            binding.completedChallengeName.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileChallengeAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_completed_challenge, parent, false)
        return ViewHolder(binding = ItemProfileCompletedChallengeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ProfileChallengeAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Challenge
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Challenge>() {
        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem == newItem
        }
    }
}