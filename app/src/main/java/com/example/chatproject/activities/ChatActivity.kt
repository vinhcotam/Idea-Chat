package com.example.chatproject.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatproject.adapters.ChatAdapter
import com.example.chatproject.databinding.ActivityChatActivityBinding
import com.example.chatproject.models.ChatMessage
import com.example.chatproject.models.User
import com.example.chatproject.network.ApiClient
import com.example.chatproject.network.ApiService
import com.example.chatproject.utilities.Constants.Companion.KEY_AVAILABILITY
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_CHAT
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_CONVERSATION
import com.example.chatproject.utilities.Constants.Companion.KEY_FCM_TOKEN
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
import com.example.chatproject.utilities.Constants.Companion.REMOTE_MSG_DATA
import com.example.chatproject.utilities.Constants.Companion.REMOTE_MSG_REGISTRATION_IDS
import com.example.chatproject.utilities.Constants.Companion.getRemoteMsgHeaders
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
            chatMessages,
            getBitmapFromEncodeString(receiverUser?.image),
            preferenceManager?.getString(KEY_USER_ID).toString()
        )

        binding.recyclerViewChat.adapter = chatAdapter
//
        database = FirebaseFirestore.getInstance()
        database?.collection(KEY_COLLECTION_USERS)?.document(receiverUser?.id.toString())?.get()
            ?.addOnCompleteListener { document ->
                if (document != null){
                    receiverUser?.image =
                        document.result.data?.get(KEY_IMAGE).toString()
                    binding.imageProfile.setImageBitmap(getBitmapFromEncodeString(receiverUser?.image))
                    chatAdapter?.setReceiverProfileImage(getBitmapFromEncodeString(receiverUser?.image))
                    chatAdapter?.notifyItemRangeChanged(0, chatMessages.size)
                }
            }
//        if(receiverUser?.image!=null){


//        }

    }

    private fun sendMessage() {
        val message: HashMap<String, Any> = HashMap()
        message[KEY_SENDER_ID] = preferenceManager?.getString(KEY_USER_ID).toString()
        message[KEY_RECEIVER_ID] = receiverUser?.id.toString()
        message[KEY_MESSAGE] = binding.editTextMessage.text.toString()
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
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser?.token)
                val data = JSONObject()
                data.put(KEY_USER_ID, preferenceManager?.getString(KEY_USER_ID))
                data.put(KEY_NAME, preferenceManager?.getString(KEY_NAME))
                data.put(KEY_FCM_TOKEN, preferenceManager?.getString(KEY_FCM_TOKEN))
                data.put(KEY_MESSAGE, binding.editTextMessage.text.toString())
                val body = JSONObject()
                body.put(REMOTE_MSG_DATA, data)
                body.put(REMOTE_MSG_REGISTRATION_IDS, tokens)
                sendNotification(body.toString())
            } catch (exception: Exception) {
                showToast(exception.message.toString())
            }
        }
        binding.editTextMessage.text.clear()

    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(messageBody: String) {
        ApiClient.getClient()?.create(ApiService::class.java)?.sendMessage(
            getRemoteMsgHeaders(), messageBody
        )?.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body().toString())
                            val results = responseJson.getJSONArray("results")
                            if (responseJson.getInt("failure") == 1) {
                                val error: JSONObject = results.get(0) as JSONObject
                                showToast(error.getString("error"))
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    showToast("Notification sent successfully")
                } else {
                    showToast("Error:" + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showToast(t.message.toString())
            }

        })

    }

    private fun listenAvailabilityOfReceiver() {
        database?.collection(KEY_COLLECTION_USERS)?.document(
            receiverUser?.id.toString()
        )?.addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                if (value.getLong(KEY_AVAILABILITY) != null) {
                    val availability: Int = Objects.requireNonNull(
                        value.getLong(KEY_AVAILABILITY)
                    )!!.toInt()
                    isReceiverAvailable = availability == 1
                }
                receiverUser?.token = value.getString(KEY_FCM_TOKEN).toString()
                if (receiverUser?.image == null) {
                    receiverUser?.image = value.getString(KEY_IMAGE).toString()
                    chatAdapter?.setReceiverProfileImage(getBitmapFromEncodeString(receiverUser?.image))
                    chatAdapter?.notifyItemRangeChanged(0, chatMessages.size)
                }
            }
            if (isReceiverAvailable) {
                binding.textViewAvailability.visibility = View.VISIBLE
            } else {
                binding.textViewAvailability.visibility = View.GONE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenMessages() {
        database?.collection(KEY_COLLECTION_CHAT)
            ?.whereEqualTo(KEY_SENDER_ID, preferenceManager?.getString(KEY_USER_ID))
            ?.whereEqualTo(KEY_RECEIVER_ID, receiverUser?.id)?.addSnapshotListener { value, error ->
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
        database?.collection(KEY_COLLECTION_CHAT)?.whereEqualTo(KEY_SENDER_ID, receiverUser?.id)
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

    private fun getBitmapFromEncodeString(encodeImage: String?): Bitmap? {
        if (encodeImage == null) return null
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
        database?.collection(KEY_CONVERSATION)?.add(conversation)?.addOnSuccessListener {
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
        database?.collection(KEY_CONVERSATION)?.whereEqualTo(KEY_SENDER_ID, senderId)
            ?.whereEqualTo(KEY_RECEIVER_ID, receiverId)?.get()?.addOnCompleteListener {
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