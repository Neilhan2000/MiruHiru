package com.neil.miruhiru.custom.item

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.ItemMyCustomChallengeBinding
import timber.log.Timber
import java.util.*


class MyCustomAdapter(val onClick: (String) -> Unit)  : ListAdapter<Challenge, MyCustomAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemMyCustomChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Challenge) {
            Glide.with(binding.customImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
            ).into(binding.customImage)
            binding.customChallengeName.text = item.name
            if (item.finished) {
                binding.isUploadedText.text = when (item.upload) {
                    true -> {
                        if (item.public) {
                            binding.isUploadedText.setBackgroundResource(R.drawable.type_public_border)
                            itemView.context.getString(R.string.isPublic)
                        } else {
                            binding.isUploadedText.setBackgroundResource(R.drawable.type_uploaded_border)
                            itemView.context.getString(R.string.verifying)
                        }
                    }
                    else -> {
                        binding.isUploadedText.setBackgroundResource(R.drawable.type_text_border)
                        itemView.context.getString(R.string.unUpoladed)
                    }
                }   
            } else {
                binding.isUploadedText.setBackgroundResource(R.drawable.type_history_border)
                binding.isUploadedText.text = itemView.context.getString(R.string.wait_for_editing)
            }
            val geoCoder = Geocoder(itemView.context, Locale.getDefault())
            val address = geoCoder.getFromLocation(item.location.latitude, item.location.longitude, 1)
            if (address.size > 0) {
                binding.locationText.text = "${address[0].countryName}, ${address[0].adminArea}"
            }
            itemView.setOnClickListener {
                onClick(item.id)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCustomAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_custom_challenge, parent, false)
        return ViewHolder(binding = ItemMyCustomChallengeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: MyCustomAdapter.ViewHolder, position: Int) {
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