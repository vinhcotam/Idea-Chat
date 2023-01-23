package com.example.chatproject.listeners

import com.example.chatproject.models.User

interface ConversationListener {
    fun onConversationClicked(user: User)
}