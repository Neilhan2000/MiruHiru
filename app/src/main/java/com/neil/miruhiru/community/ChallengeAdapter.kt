package com.neil.miruhiru.community

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
import com.neil.miruhiru.databinding.ItemCommunityChallengeBinding
import com.neil.miruhiru.util.Util.getString
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
            if (item.commentQuantity.toInt() != 0) {
                binding.communityChallengeRating.text =
                    "${roundOffDecimal(item.totalRating)} (${item.commentQuantity})"
            } else {
                binding.communityChallengeRating.text = getString(R.string.no_comment)
            }
            val geoCoder = Geocoder(itemView.context, Locale.getDefault())
            val address = geoCoder.getFromLocation(item.location.latitude, item.location.longitude, 1)
            if (address.size > 0) {
                if (address[0].adminArea != null) {
                    binding.communityChallengeLocation.text = "${address[0].countryName}, ${address[0].adminArea}"
                } else {
                    binding.communityChallengeLocation.text = "${address[0].countryName}"
                }
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