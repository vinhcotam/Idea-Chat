package com.example.chatproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.chatproject.adapters.UserAdapter
import com.example.chatproject.databinding.ActivityUserBinding
import com.example.chatproject.listeners.UserListener
import com.example.chatproject.models.User
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_EMAIL
import com.example.chatproject.utilities.Constants.Companion.KEY_FCM_TOKEN
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_USER
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UserActivity : AppCompatActivity(), UserListener {
    private val binding by lazy {
        ActivityUserBinding.inflate(layoutInflater)
    }
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListener()
        getUser()

    }

    private fun setListener() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUser() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val currentUserId = preferenceManager?.getString(KEY_USER_ID)
                if (it.isSuccessful && it.result != null) {
                    val users: MutableList<User> = ArrayList()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result) {
                        if (currentUserId.equals(queryDocumentSnapshot.id)) {
                            continue
                        }
                        val name =queryDocumentSnapshot.getString(KEY_NAME).toString()
                        val email = queryDocumentSnapshot.getString(KEY_EMAIL).toString()
                        val image = queryDocumentSnapshot.getString(KEY_IMAGE).toString()
                        val token = queryDocumentSnapshot.getString(KEY_FCM_TOKEN).toString()
                        val id = queryDocumentSnapshot.id
                        val user = User()
                        user.name = name
                        user.email = email
                        user.image = image
                        user.token = token
                        user.id = id
                        users.add(user)
                    }
                    if (users.isNotEmpty()) {
                        val userAdapter = UserAdapter(users, this)
                        binding.recyclerViewUser.adapter = userAdapter
                        binding.recyclerViewUser.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage() {
        binding.textViewErrorMessage.text = String.format("%s", "No user available")
        binding.textViewErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.processBar.visibility = View.VISIBLE
        } else {
            binding.processBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(KEY_USER, user)
        startActivity(intent)
        finish()
    }
}

