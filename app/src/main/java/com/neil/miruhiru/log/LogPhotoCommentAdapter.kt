package com.neil.miruhiru.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.R
import com.neil.miruhiru.data.PhotoComment
import com.neil.miruhiru.databinding.ItemLogPhotoCommentBinding

class LogPhotoCommentAdapter: ListAdapter<PhotoComment, LogPhotoCommentAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemLogPhotoCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhotoComment) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogPhotoCommentAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_photo_comment, parent, false)
        return ViewHolder(binding = ItemLogPhotoCommentBinding.bind(view))
    }

    override fun onBindViewHolder(holder: LogPhotoCommentAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as PhotoComment
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<PhotoComment>() {
        override fun areItemsTheSame(oldItem: PhotoComment, newItem: PhotoComment): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PhotoComment, newItem: PhotoComment): Boolean {
            return oldItem == newItem
        }
    }
}