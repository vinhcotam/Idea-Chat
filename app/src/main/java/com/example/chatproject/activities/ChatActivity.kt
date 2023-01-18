package com.example.chatproject.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.example.chatproject.adapters.ChatAdapter
import com.example.chatproject.databinding.ActivityChatActivityBinding
import com.example.chatproject.models.ChatMessage
import com.example.chatproject.models.User
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_CHAT
import com.example.chatproject.utilities.Constants.Companion.KEY_MESSAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_ID
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_ID
import com.example.chatproject.utilities.Constants.Companion.KEY_TIMESTAMP

import com.example.chatproject.utilities.Constants.Companion.KEY_USER
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityChatActivityBinding.inflate(layoutInflater)
    }
    private var receiverUser: User? = null
    private var chatMessages = ArrayList<ChatMessage>()
    private var chatAdapter: ChatAdapter? = null
    private var preferenceManager: PreferenceManager? = null
    private var database: FirebaseFirestore? = null
    private var chatMessage = ChatMessage()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages, getBitmapFromEncodeString(receiverUser?.image),
            preferenceManager?.getString(KEY_USER_ID).toString()
        )
        binding.recyclerViewChat.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()

    }

    private fun sendMessage() {
        val message: HashMap<String, String> = HashMap()
        message[KEY_SENDER_ID] = preferenceManager?.getString(KEY_USER_ID).toString()
        message[KEY_RECEIVER_ID] = receiverUser?.id.toString()
        message[KEY_MESSAGE] = binding.editTextMessage.text.toString()
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val current = formatter.format(time)
//        val calendar = Calendar.getInstance()
//        val current = LocalDateTime.of(
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH),
//            calendar.get(Calendar.HOUR_OF_DAY),
//            calendar.get(Calendar.MINUTE),
//            calendar.get(Calendar.SECOND)
//        )
        message[KEY_TIMESTAMP] = current.toString()
        database?.collection(KEY_COLLECTION_CHAT)?.add(message)
        binding.editTextMessage.text.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenMessages() {
        database?.collection(KEY_COLLECTION_CHAT)
            ?.whereEqualTo(KEY_SENDER_ID, preferenceManager?.getString(KEY_USER_ID))
            ?.whereEqualTo(KEY_RECEIVER_ID, receiverUser?.id)
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val count = chatMessages.size
                    for (documentChange: DocumentChange in value.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            chatMessage = ChatMessage()
                            chatMessage.senderId =
                                documentChange.document.getString(KEY_SENDER_ID).toString()
                            chatMessage.receiverId =
                                documentChange.document[KEY_RECEIVER_ID].toString()
                            chatMessage.message = documentChange.document[KEY_MESSAGE].toString()
                            chatMessage.dateTime = documentChange.document[KEY_TIMESTAMP].toString()

//                            chatMessage.dateTime = getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP)!!)
//                            chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)!!

                            chatMessages.add(chatMessage)
                        }
                    }
                    chatMessages.sortBy{ it. dateTime}
                    if (count == 0) {
                        chatAdapter?.notifyDataSetChanged()
                    } else {
                        chatAdapter?.notifyItemRangeInserted(
                            chatMessages.size, chatMessages.size)
                        binding.recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.recyclerViewChat.visibility = View.VISIBLE
                }
                binding.processBar.visibility = View.GONE
            }
        database?.collection(KEY_COLLECTION_CHAT)
            ?.whereEqualTo(KEY_SENDER_ID, receiverUser?.id)
            ?.whereEqualTo(KEY_RECEIVER_ID, preferenceManager?.getString(KEY_USER_ID))
            ?.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val count = chatMessages.size
                    for (documentChange: DocumentChange in value.documentChanges) {
                        if (documentChange.type == DocumentChange.Type.ADDED) {
                            chatMessage = ChatMessage()
                            chatMessage.senderId =
                                documentChange.document.getString(KEY_SENDER_ID).toString()
                            chatMessage.receiverId =
                                documentChange.document[KEY_RECEIVER_ID].toString()
                            chatMessage.message = documentChange.document[KEY_MESSAGE].toString()
                            chatMessage.dateTime = documentChange.document[KEY_TIMESTAMP].toString()
//                            chatMessage.dateTime = getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP)!!)
//                            chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)!!
                            chatMessages.add(chatMessage)
                        }
                    }
                    chatMessages.sortBy{ it. dateTime}
                    if (count == 0) {
                        chatAdapter?.notifyDataSetChanged()
                    } else {
                        chatAdapter?.notifyItemRangeInserted(
                            chatMessages.size, chatMessages.size)
                        binding.recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.recyclerViewChat.visibility = View.VISIBLE
                }
                binding.processBar.visibility = View.GONE
            }
    }

    private fun getBitmapFromEncodeString(encodeImage: String?): Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(KEY_USER) as? User
        binding.textViewName.text = receiverUser?.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy -hh:mm a", Locale.getDefault()).format(date)
    }
}