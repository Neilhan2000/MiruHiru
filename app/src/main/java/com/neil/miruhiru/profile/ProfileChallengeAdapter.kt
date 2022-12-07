package com.neil.miruhiru.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
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


class ProfileChallengeAdapter(val onclick: (Int) -> Unit) : ListAdapter<Challenge, ProfileChallengeAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemProfileCompletedChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Challenge) {
            Glide.with(binding.completedChallengeImage.context).load(item.image).circleCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_challenge_success).error(R.drawable.ic_challenge_success)
            ).into(binding.completedChallengeImage)
            binding.completedChallengeName.text = item.name
            itemView.setOnClickListener {
                onclick(adapterPosition)
            }

            // set first item left margin
            if (adapterPosition == 0) {
                (binding.completedChallengeImage.layoutParams as ConstraintLayout.LayoutParams).apply {
                    marginStart = dpToPixel(16)
                    topMargin = dpToPixel(0)
                    marginEnd = dpToPixel(8)
                    bottomMargin = dpToPixel(0)
                }
            } else {
                (binding.completedChallengeImage.layoutParams as ConstraintLayout.LayoutParams).apply {
                    marginStart = dpToPixel(8)
                    topMargin = dpToPixel(0)
                    marginEnd = dpToPixel(8)
                    bottomMargin = dpToPixel(0)
                }
            }
        }

        private fun dpToPixel(dp: Int): Int {
            val density = itemView.context.resources.displayMetrics.density
            return (dp * density).toInt()
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