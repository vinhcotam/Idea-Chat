package com.example.chatproject.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatproject.databinding.ItemContainerReceivedMessageBinding
import com.example.chatproject.databinding.ItemContainerSentMessageBinding
import com.example.chatproject.models.ChatMessage

class ChatAdapter(chatMessages: List<ChatMessage>,
                  private var receiverProfileImage: Bitmap?,
                  private val senderId: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val chatList = chatMessages
    private val chatMessage = chatList


    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }
    fun setReceiverProfileImage(bitmap: Bitmap?){
        receiverProfileImage = bitmap
    }
    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.textViewMessage.text = chatMessage.message
            binding.textViewDatetime.text = chatMessage.dateTime
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage
                    , receiverProfileImage: Bitmap?
        ) {
            binding.textViewMessage.text = chatMessage.message
            binding.textViewDatetime.text = chatMessage.dateTime
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessage[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(chatMessage[position]
                , receiverProfileImage
            )
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].senderId == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
}