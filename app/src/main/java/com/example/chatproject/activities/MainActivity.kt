package com.example.chatproject.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatproject.adapters.RecentConversationsAdapter
import com.example.chatproject.databinding.ActivityMainBinding
import com.example.chatproject.listeners.ConversationListener
import com.example.chatproject.models.ChatMessage
import com.example.chatproject.models.User
import com.example.chatproject.utilities.Constants
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_CONVERSATION
import com.example.chatproject.utilities.Constants.Companion.KEY_FCM_TOKEN
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_LAST_MESSAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_ID
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_ID
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_TIMESTAMP
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : BaseActivity(), ConversationListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var preferenceManager: PreferenceManager? = null
    private var conversations = ArrayList<ChatMessage>()
    private var conversationsAdapter: RecentConversationsAdapter? = null
    private var database: FirebaseFirestore? = null
    private var chatMessage = ChatMessage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager((this))
        init()
        loadUserDetail()
        getToken()
        setListener()
        listenConversations()
    }

    private fun init() {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
        binding.recyclerViewConversation.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListener() {
        binding.imageSignOut.setOnClickListener { signOut() }
        binding.fabNewChat.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadUserDetail() {
        binding.textViewName.text = preferenceManager?.getString(KEY_NAME)
        val bytes = Base64.decode(preferenceManager?.getString(KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun listenConversations() {
        database?.collection(KEY_CONVERSATION)
            ?.whereEqualTo(KEY_SENDER_ID, preferenceManager?.getString(KEY_USER_ID))
            ?.addSnapshotListener(eventListener)

        database?.collection(KEY_CONVERSATION)
            ?.whereEqualTo(KEY_RECEIVER_ID, preferenceManager?.getString(KEY_USER_ID))
            ?.addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener =
        addSnapshotListener@{ value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val senderId = documentChange.document.getString(KEY_SENDER_ID)
                        val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                        chatMessage = ChatMessage()
                        chatMessage.senderId = senderId.toString()
                        chatMessage.receiverId = receiverId.toString()
                        if (preferenceManager?.getString(KEY_USER_ID).toString() == senderId) {
                            chatMessage.conversionImage =
                                documentChange.document.getString(KEY_RECEIVER_IMAGE).toString()
                            chatMessage.conversionName =
                                documentChange.document.getString(KEY_RECEIVER_NAME).toString()
                            chatMessage.conversionId =
                                documentChange.document.getString(KEY_RECEIVER_ID).toString()
                        } else {
                            chatMessage.conversionImage =
                                documentChange.document.getString(KEY_SENDER_IMAGE).toString()
                            chatMessage.conversionName =
                                documentChange.document.getString(KEY_SENDER_NAME).toString()
                            chatMessage.conversionId =
                                documentChange.document.getString(KEY_SENDER_ID).toString()
                        }
                        chatMessage.message =
                            documentChange.document.getString(KEY_LAST_MESSAGE).toString()
                        chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)!!
                        conversations.add(chatMessage)
                    } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                        for (i in 0 until conversations.size) {
                            val senderId = documentChange.document.getString(KEY_SENDER_ID)
                            val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)
                            if (conversations[i].senderId == senderId && conversations[i].receiverId == receiverId) {
                                conversations[i].message =
                                    documentChange.document.getString(KEY_LAST_MESSAGE).toString()
                                chatMessage.dateObject =
                                    documentChange.document.getDate(KEY_TIMESTAMP)!!
                                break
                            }
                        }
                    }
                }
                conversations.sortByDescending { it.dateObject }
//                conversations.sortWith(compareBy { it.dateObject })
//                conversations.sortedByDescending { it.dateObject }
                conversationsAdapter?.notifyDataSetChanged()
                binding.recyclerViewConversation.smoothScrollToPosition(0)
                binding.recyclerViewConversation.visibility = View.VISIBLE
                binding.processBar.visibility = View.GONE
            }
        }


    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            updateToken(it)
        }
    }

    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager?.getString(KEY_USER_ID).toString()
        )
        documentReference.update(KEY_FCM_TOKEN, token)
//            .addOnSuccessListener { showToast("Token updated successfully") }
            .addOnFailureListener { showToast("Unable to update token") }
    }

    private fun signOut() {
        showToast("Signing out ...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager?.getString(KEY_USER_ID).toString()
        )
        val updates: HashMap<String, Any> = HashMap()
        updates[KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager?.clear()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to sign out")
            }
    }

    override fun onConversationClicked(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)

    }

}