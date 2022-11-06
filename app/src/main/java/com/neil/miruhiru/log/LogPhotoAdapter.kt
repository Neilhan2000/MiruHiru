package com.neil.miruhiru.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Photo
import com.neil.miruhiru.data.PhotoComment
import com.neil.miruhiru.databinding.ItemLogPhotoBinding

class LogPhotoAdapter: ListAdapter<Photo, LogPhotoAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemLogPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Photo) {
            val photoCommentList = listOf(PhotoComment(), PhotoComment())
            val photoCommentAdapter = LogPhotoCommentAdapter()
            binding.recyclerPhotoComment.adapter = photoCommentAdapter
            photoCommentAdapter.submitList(photoCommentList)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogPhotoAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_photo, parent, false)
        return ViewHolder(binding = ItemLogPhotoBinding.bind(view))
    }

    override fun onBindViewHolder(holder: LogPhotoAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Photo
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }
}