package com.example.chatproject.listeners

import com.example.chatproject.models.User

interface UserListener {
    fun onUserClicked(user: User)
}