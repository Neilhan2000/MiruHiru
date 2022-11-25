//package com.neil.miruhiru.verify
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.neil.miruhiru.R
//import com.neil.miruhiru.data.Challenge
//import com.neil.miruhiru.databinding.ItemVerifyTaskBinding
//import com.neil.miruhiru.verifydetail.VerifyTaskAdapter
//
//class VerifyChallengeAdapter(val onclick: (String) -> Unit) : ListAdapter<Challenge, VerifyTaskAdapter.ViewHolder>(DiffCallBack()) {
//
//    inner class ViewHolder(private val binding: ItemVerifyTaskBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: Challenge) {
//            Glide.with(binding.verifyImage.context).load(item.image).centerCrop().apply(
//                RequestOptions().placeholder(R.drawable.image_placeholder).error(R.drawable.image_placeholder)
//            ).into(binding.verifyImage)
//            itemView.setOnClickListener {
//                onclick(item.id)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifyTaskAdapter.ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_verify_task, parent, false)
//        return ViewHolder(binding = ItemVerifyTaskBinding.bind(view))
//    }
//
//    override fun onBindViewHolder(holder: VerifyTaskAdapter.ViewHolder, position: Int) {
//        val data = getItem(position) as Challenge
//        holder.bind(data)
//    }
//
//    class DiffCallBack : DiffUtil.ItemCallback<Challenge>() {
//        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
//            return oldItem == newItem
//        }
//
//        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
//            return oldItem == newItem
//        }
//    }
//}