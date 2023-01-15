package com.example.chatproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.chatproject.adapters.UserAdapter
import com.example.chatproject.databinding.ActivityUserBinding
import com.example.chatproject.models.User
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_EMAIL
import com.example.chatproject.utilities.Constants.Companion.KEY_FCM_TOKEN
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UserActivity : AppCompatActivity() {
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
                        val user = User(
                            queryDocumentSnapshot.getString(KEY_NAME).toString(),
                            queryDocumentSnapshot.getString(KEY_EMAIL).toString(),
                            queryDocumentSnapshot.getString(KEY_IMAGE).toString(),
                            queryDocumentSnapshot.getString(KEY_FCM_TOKEN).toString()
                        )
//                        user?.name = queryDocumentSnapshot.getString(KEY_NAME).toString()
//                        user?.email = queryDocumentSnapshot.getString(KEY_EMAIL).toString()
//                        user?.image = queryDocumentSnapshot.getString(KEY_IMAGE).toString()
//                        user?.token = queryDocumentSnapshot.getString(KEY_FCM_TOKEN).toString()
                        users.add(user)
                    }
                    if (users.isNotEmpty()) {
                        val userAdapter = UserAdapter(users)
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
}