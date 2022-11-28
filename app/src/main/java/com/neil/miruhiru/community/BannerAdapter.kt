package com.neil.miruhiru.community

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.ItemCommunityBannerBinding
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder

class BannerAdapter : BaseBannerAdapter<Challenge>()  {
    override fun bindData(
        holder: BaseViewHolder<Challenge>?,
        data: Challenge?,
        position: Int,
        pageSize: Int
    ) {
        val binding = holder?.itemView?.let { ItemCommunityBannerBinding.bind(it) }

        binding?.bannerImage?.context?.let {
            Glide.with(it).load(data?.image).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
            ).into(binding.bannerImage)
            binding.bannerTitle.text = data?.name
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_community_banner
    }
}