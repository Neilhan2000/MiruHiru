package com.neil.miruhiru.challengedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Comment
import com.neil.miruhiru.databinding.ItemChallengeCommentBinding


class CommentAdapter(
    viewModel: ChallengeDetailViewModel,
    val onClick: (Int) -> Unit
): ListAdapter<Comment, CommentAdapter.ViewHolder>(DiffCallBack()) {

    private val viewModel = viewModel

    inner class ViewHolder(private val binding: ItemChallengeCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Comment) {
            binding.ratingBar.rating = item.rating
            binding.comment.text = item.text

            var userIcon = ""
            var userName = ""
            viewModel.commentUsers.value?.forEach { user ->
                if (item.userId == user.id) {
                    userIcon = user.icon
                    userName = user.name
                }
            }

            binding.userName.text = userName
            Glide.with(binding.userIcon.context).load(userIcon).circleCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
            ).into(binding.userIcon)
            if (item.userId != UserManager.userId) {
                binding.reportIcon.setOnClickListener {
                    onClick(adapterPosition)
                }
            } else {
                binding.reportIcon.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge_comment, parent, false)
        return ViewHolder(binding = ItemChallengeCommentBinding.bind(view))
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Comment
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}