package com.example.chatproject.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatproject.databinding.ItemContainerRecentConversationsBinding
import com.example.chatproject.listeners.ConversationListener

import com.example.chatproject.models.ChatMessage
import com.example.chatproject.models.User

class RecentConversationsAdapter(chatMessages: List<ChatMessage>, mListener: ConversationListener) :
    RecyclerView.Adapter<RecentConversationsAdapter.ViewHolder>() {

    private val chatMessages = chatMessages
    private var mListener:ConversationListener = mListener

    class ViewHolder(private val binding: ItemContainerRecentConversationsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private fun getConversionImage(encodeImage: String): Bitmap {
            val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        fun setData(chatMessage: ChatMessage, mListener: ConversationListener) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage))
            binding.textViewName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener{
                val user = User()
                user.id = chatMessage.conversionId
                user.name = chatMessage.conversionName
                user.image = chatMessage.conversionImage
                mListener.onConversationClicked(user)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerRecentConversationsBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(chatMessages[position], mListener)
    }

    override fun getItemCount(): Int {
       return  chatMessages.size
    }
}