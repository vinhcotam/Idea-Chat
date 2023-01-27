package com.example.chatproject.models

import java.io.Serializable
import java.util.*

class ChatMessage : Serializable {
    lateinit var senderId: String
    lateinit var receiverId: String
    lateinit var message: String
    lateinit var dateTime: String
    lateinit var dateObject: Date
    lateinit var conversionId: String
    lateinit var conversionName: String
    lateinit var conversionImage: String
}