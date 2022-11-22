package com.neil.miruhiru.customdetail

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.custom.item.MyCustomAdapter
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Feature
import com.neil.miruhiru.databinding.ItemMyCustomChallengeBinding
import com.neil.miruhiru.databinding.ItemSearchResultBinding
import java.util.*

class SearchResultAdapter(val onClick: (Int) -> Unit)  : ListAdapter<Feature, SearchResultAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Feature) {
            binding.resultText.text = item.placeName
            itemView.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(binding = ItemSearchResultBinding.bind(view))
    }

    override fun onBindViewHolder(holder: SearchResultAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Feature
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Feature>() {
        override fun areItemsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem == newItem
        }
    }
}