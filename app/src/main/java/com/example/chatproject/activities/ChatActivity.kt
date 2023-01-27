package com.example.chatproject.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.example.chatproject.adapters.ChatAdapter
import com.example.chatproject.databinding.ActivityChatActivityBinding
import com.example.chatproject.models.ChatMessage
import com.example.chatproject.models.User
import com.example.chatproject.utilities.Constants.Companion.KEY_AVAILABILITY
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_CHAT
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_CONVERSATION
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_LAST_MESSAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_MESSAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_ID
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_RECEIVER_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_ID
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_SENDER_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_TIMESTAMP

import com.example.chatproject.utilities.Constants.Companion.KEY_USER
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : BaseActivity() {
    private val binding by lazy {
        ActivityChatActivityBinding.inflate(layoutInflater)
    }
    private var receiverUser: User? = null
    private var chatMessages = ArrayList<ChatMessage>()
    private var chatAdapter: ChatAdapter? = null
    private var preferenceManager: PreferenceManager? = null
    private var database: FirebaseFirestore? = null
    private var chatMessage = ChatMessage()
    private var conversionId: String? = null
    private var isReceiverAvailable = false
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

    @SuppressLint("SimpleDateFormat")
    private fun sendMessage() {
        val message: HashMap<String, Any> = HashMap()
        message[KEY_SENDER_ID] = preferenceManager?.getString(KEY_USER_ID).toString()
        message[KEY_RECEIVER_ID] = receiverUser?.id.toString()
        message[KEY_MESSAGE] = binding.editTextMessage.text.toString()

//        "MMMM dd, yyyy -hh:mm a"
//        message[KEY_TIMESTAMP] = current.toString()
        message[KEY_TIMESTAMP] = Date()

        database?.collection(KEY_COLLECTION_CHAT)?.add(message)
        if (conversionId != null) {
            updateConversation(binding.editTextMessage.text.toString())
        } else {
            val conversation: HashMap<String, Any> = HashMap()
            conversation[KEY_SENDER_ID] = preferenceManager?.getString(KEY_USER_ID).toString()
            conversation[KEY_SENDER_NAME] = preferenceManager?.getString(KEY_NAME).toString()
            conversation[KEY_SENDER_IMAGE] = preferenceManager?.getString(KEY_IMAGE).toString()
            conversation[KEY_RECEIVER_ID] = receiverUser?.id.toString()
            conversation[KEY_RECEIVER_NAME] = receiverUser?.name.toString()
            conversation[KEY_RECEIVER_IMAGE] = receiverUser?.image.toString()
            conversation[KEY_LAST_MESSAGE] = binding.editTextMessage.text.toString()
            conversation[KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        binding.editTextMessage.text.clear()

    }

    private fun listenAvailabilityOfReceiver() {
        database?.collection(KEY_COLLECTION_USERS)?.document(
            receiverUser?.id.toString()
        )?.addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                val availability: Int = Objects.requireNonNull(
                    value.getLong(KEY_AVAILABILITY)
                )!!.toInt()
                isReceiverAvailable = availability ==1
            }
            if(isReceiverAvailable){
                binding.textViewAvailability.visibility = View.VISIBLE
            }else{
                binding.textViewAvailability.visibility = View.GONE
            }
        }
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
//                            chatMessage.dateTime = documentChange.document[KEY_TIMESTAMP].toString()
                            chatMessage.dateTime =
                                getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP))
//                            chatMessage.dateTime = getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP)!!)
                            chatMessage.dateObject =
                                documentChange.document.getDate(KEY_TIMESTAMP)!!

                            chatMessages.add(chatMessage)
                        }
                    }
                    chatMessages.sortBy { it.dateObject }
                    if (count == 0) {
                        chatAdapter?.notifyDataSetChanged()
                    } else {
                        chatAdapter?.notifyItemRangeInserted(
                            chatMessages.size, chatMessages.size
                        )
                        binding.recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.recyclerViewChat.visibility = View.VISIBLE
                }
                binding.processBar.visibility = View.GONE
                if (conversionId == null) {
                    checkForConversation()
                }
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
                            chatMessage.dateTime =
                                getReadableDateTime(documentChange.document.getDate(KEY_TIMESTAMP))
//                            chatMessage.dateTime = documentChange.document[KEY_TIMESTAMP].toString()
                            chatMessage.dateObject =
                                documentChange.document.getDate(KEY_TIMESTAMP)!!
                            chatMessages.add(chatMessage)
                        }
                    }
                    chatMessages.sortBy { it.dateObject }
                    if (count == 0) {
                        chatAdapter?.notifyDataSetChanged()
                    } else {
                        chatAdapter?.notifyItemRangeInserted(
                            chatMessages.size, chatMessages.size
                        )
                        binding.recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.recyclerViewChat.visibility = View.VISIBLE
                }
                binding.processBar.visibility = View.GONE
                if (conversionId == null) {
                    checkForConversation()
                }
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

    private fun addConversation(conversation: HashMap<String, Any>) {
        database?.collection(KEY_CONVERSATION)
            ?.add(conversation)
            ?.addOnSuccessListener {
                conversionId = it.id
            }

    }

    private fun updateConversation(message: String) {
        val documentReference =
            database?.collection(KEY_CONVERSATION)?.document(conversionId.toString())
        documentReference?.update(KEY_LAST_MESSAGE, message, KEY_TIMESTAMP, Date())
    }

    private fun checkForConversation() {
        if (chatMessages.size != 0) {
            preferenceManager?.getString(KEY_USER_ID)
                ?.let { receiverUser?.id?.let { it1 -> checkForConversationRemotely(it, it1) } }
            preferenceManager?.getString(KEY_USER_ID)
                ?.let { receiverUser?.id?.let { it1 -> checkForConversationRemotely(it1, it) } }
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database?.collection(KEY_CONVERSATION)
            ?.whereEqualTo(KEY_SENDER_ID, senderId)
            ?.whereEqualTo(KEY_RECEIVER_ID, receiverId)
            ?.get()
            ?.addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result.documents.size > 0) {
                    val documentSnapshot: DocumentSnapshot = it.result.documents[0]
                    conversionId = documentSnapshot.id

                }
            }
    }

    private fun getReadableDateTime(date: Date?): String {
        return date?.let {
            SimpleDateFormat("MMMM dd, yyyy - hh:mm:ss a", Locale.getDefault()).format(
                it
            )
        }.toString()
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }

}