package com.example.chatproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatproject.databinding.ActivityChatActivityBinding

class ChatActivity : AppCompatActivity() {
    private val binding by lazy{
        ActivityChatActivityBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}