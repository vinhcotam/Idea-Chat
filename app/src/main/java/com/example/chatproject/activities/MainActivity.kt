package com.example.chatproject.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.chatproject.databinding.ActivityMainBinding
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_FCM_TOKEN
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager((this))
        loadUserDetail()
        getToken()
        setListener()
    }

    private fun setListener() {
        binding.imageSignOut.setOnClickListener { signOut() }
        binding.fabNewChat.setOnClickListener{
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
}