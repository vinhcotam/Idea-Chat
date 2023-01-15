package com.example.chatproject.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatproject.databinding.ItemContainerUserBinding
import com.example.chatproject.models.User

class UserAdapter(items: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private val listUsers = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listUsers[position])

    }

    class ViewHolder(private val binding: ItemContainerUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var user: User? = null

        private fun getUserImage(encodeImage: String): Bitmap? {
            val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        fun bind(items: User) {
            user = items
            binding.textViewName.text = items.name
            binding.textViewEmail.text = items.email
            binding.imageProfile.setImageBitmap(getUserImage(items.image))

        }
    }

}