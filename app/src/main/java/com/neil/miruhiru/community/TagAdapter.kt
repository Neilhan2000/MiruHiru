package com.neil.miruhiru.community

import android.annotation.SuppressLint
import android.location.Geocoder
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.rpc.context.AttributeContext
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.ItemCommunityTagBinding
import com.neil.miruhiru.databinding.ItemLikeChallengeBinding
import com.neil.miruhiru.likeChallenge.LikeChallengeAdapter
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class TagAdapter(val onclick: (String) -> Unit) : ListAdapter<String, TagAdapter.ViewHolder>(DiffCallBack()) {

    // set first item to be selected
    private var selectedPosition = 0


    inner class ViewHolder(private val binding: ItemCommunityTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(item: String) {
            binding.tag.text = item
            itemView.setOnClickListener {
                selectedPosition = adapterPosition
                onclick(item)
                notifyDataSetChanged()
            }

            // using selected position to avoid losing data in rebinding view
            if (adapterPosition != selectedPosition) {
                binding.tag.setBackgroundResource(R.drawable.community_tag_border)
                binding.tag.setTextColor(
                    (itemView.context.getResources().getColor(R.color.black))
                )
                binding.tag.setPadding(dpToPixel(20), dpToPixel(3), dpToPixel(20), dpToPixel(3))
            } else {
                binding.tag.setBackgroundResource(R.drawable.community_tag_border_selected)
                binding.tag.setTextColor((itemView.context.getResources().getColor(R.color.white)))
                binding.tag.setPadding(dpToPixel(20), dpToPixel(3), dpToPixel(20), dpToPixel(3))
            }

        }

        fun dpToPixel(dp: Int): Int {
            val density = itemView.context.resources.displayMetrics.density
            return (dp * density).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_tag, parent, false)
        return ViewHolder(binding = ItemCommunityTagBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TagAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as String
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}