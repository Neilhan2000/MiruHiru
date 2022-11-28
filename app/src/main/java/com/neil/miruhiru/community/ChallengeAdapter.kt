package com.neil.miruhiru.community

import android.annotation.SuppressLint
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.ItemCommunityChallengeBinding
import com.neil.miruhiru.databinding.ItemCommunityTagBinding
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class ChallengeAdapter(val onclick: (String) -> Unit) : ListAdapter<Challenge, ChallengeAdapter.ViewHolder>(DiffCallBack()) {

    inner class ViewHolder(private val binding: ItemCommunityChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Challenge) {
            Glide.with(binding.CommunityChallengeImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
            ).into(binding.CommunityChallengeImage)
            binding.communityChallnegeName.text = item.name
            binding.communityChallengeRating.text = "${roundOffDecimal(item.totalRating)} (${item.commentQuantity})"
            val geoCoder = Geocoder(itemView.context, Locale.getDefault())
            val address = geoCoder.getFromLocation(item.location.latitude, item.location.longitude, 1)
            if (address.size > 0) {
                binding.communityChallengeLocation.text = "${address[0].countryName}, ${address[0].adminArea}"
            }
            itemView.setOnClickListener {
                onclick(item.id)
            }
        }

        private fun roundOffDecimal(number: Float): Float? {
            val df = DecimalFormat("#.#")
            df.roundingMode = RoundingMode.CEILING
            return df.format(number).toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_challenge, parent, false)
        return ViewHolder(binding = ItemCommunityChallengeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ChallengeAdapter.ViewHolder, position: Int) {
        val data = getItem(position) as Challenge
        holder.bind(data)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Challenge>() {
        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem == newItem
        }
    }
}