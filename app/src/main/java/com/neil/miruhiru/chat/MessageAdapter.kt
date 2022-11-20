package com.neil.miruhiru.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Message
import com.neil.miruhiru.databinding.ItemChatOtherMessageBinding
import com.neil.miruhiru.databinding.ItemMyMessageBinding

class MessageAdapter(viewModel: ChatDialogViewModel) : ListAdapter<MessageAdapter.MessageItem, RecyclerView.ViewHolder>(DiffCallBack()) {

    private val viewModel = viewModel

    enum class MessageType {
        ITEM_MY_MESSAGE,
        ITEM_OTHER_MESSAGE,
    }

    sealed class MessageItem {
        data class MyMessage(val message: Message) : MessageItem() {}
        data class OtherMessage(val message: Message) : MessageItem() {}
    }

    inner class MyMessageViewHolder(private val binding: ItemMyMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {

            viewModel.memberList.forEach {
                if (it.id == item.senderId) {
                    Glide.with(binding.userIconMine.context).load(it.icon).circleCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_user_no_photo).error(R.drawable.ic_user_no_photo)
                    ).into(binding.userIconMine)
                }
            }
            binding.myMessage.text = item.text
        }
    }

    inner class OtherMessageViewHolder(private val binding: ItemChatOtherMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Message) {
            viewModel.memberList.forEach {
                if (it.id == item.senderId) {
                    Glide.with(binding.userIconOther.context).load(it.icon).circleCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
                    ).into(binding.userIconOther)
                }
            }
            binding.otherMessage.text = item.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.ITEM_MY_MESSAGE.ordinal -> MyMessageViewHolder(
                ItemMyMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            MessageType.ITEM_OTHER_MESSAGE.ordinal -> OtherMessageViewHolder(
                ItemChatOtherMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyMessageViewHolder -> {
                holder.bind((getItem(position) as MessageItem.MyMessage).message)
            }
            is OtherMessageViewHolder -> {
                holder.bind((getItem(position) as MessageItem.OtherMessage).message)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MessageItem.MyMessage -> MessageType.ITEM_MY_MESSAGE.ordinal
            else -> MessageType.ITEM_OTHER_MESSAGE.ordinal
        }
    }

    class DiffCallBack : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem == newItem
        }
    }
}