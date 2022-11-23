package com.neil.miruhiru.likeChallenge

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
import com.neil.miruhiru.databinding.ItemLikeChallengeBinding
import com.neil.miruhiru.databinding.ItemProfileCompletedChallengeBinding
import com.neil.miruhiru.profile.ProfileChallengeAdapter
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class LikeChallengeAdapter(val onclick: (String) -> Unit) : ListAdapter<Challenge, LikeChallengeAdapter.ViewHolder>(DiffCallBack()) {


    inner class ViewHolder(private val binding: ItemLikeChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Challenge) {
            Glide.with(binding.likeChallengeImage.context).load(item.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_challenge_success).error(R.drawable.ic_challenge_success)
            ).into(binding.likeChallengeImage)
            binding.likChallengeName.text = item.name
            binding.likeChallengeRating.rating = item.totalRating
            binding.likeChallengeRatingText.text = "${roundOffDecimal(item.totalRating)} (${item.commentQuantity})"
            val geoCoder = Geocoder(itemView.context, Locale.getDefault())
            val address = geoCoder.getFromLocation(item.location.latitude, item.location.longitude, 1)
            if (address.size > 0) {
                binding.likeChallengeLocation.text = "${address[0].countryName}, ${address[0].adminArea}"
            }
            binding.likeChallengeTag.text = item.type
            when (item.type) {
                itemView.context.getString(R.string.food) -> binding.likeChallengeTag.setBackgroundResource(R.drawable.type_text_border)
                itemView.context.getString(R.string.couple) -> binding.likeChallengeTag.setBackgroundResource(R.drawable.type_couple_border)
                itemView.context.getString(R.string.history) -> binding.likeChallengeTag.setBackgroundResource(R.drawable.type_history_border)
                itemView.context.getString(R.string.travel) -> binding.likeChallengeTag.setBackgroundResource(R.drawable.type_travel_border)
                itemView.context.getString(R.string.special) -> binding.likeChallengeTag.setBackgroundResource(R.drawable.type_special_border)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeChallengeAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_like_challenge, parent, false)
        return ViewHolder(binding = ItemLikeChallengeBinding.bind(view))
    }

    override fun onBindViewHolder(holder: LikeChallengeAdapter.ViewHolder, position: Int) {
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