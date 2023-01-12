package com.example.chatproject.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chatproject.databinding.ActivitySignInBinding
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_EMAIL
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_IS_SIGNED_IN
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_PASSWORD
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class SignInActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager((this))
        if(preferenceManager!!.getBoolean(KEY_IS_SIGNED_IN)){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setListener()
    }

    private fun setListener() {
        binding.textViewCreateNewAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails()) signIn()
        }

    }

    private fun signIn() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .whereEqualTo(KEY_EMAIL, binding.editTextEmail.text.toString())
            .whereEqualTo(KEY_PASSWORD, binding.editTextPassword.text.toString())
            .get()
            .addOnCompleteListener() {
                if (it.isSuccessful && it.result != null
                    && it.result.documents.size > 0) {
                    val documentSnapShot: DocumentSnapshot = it.result.documents[0]
                    preferenceManager?.putBoolean(KEY_IS_SIGNED_IN, true)
                    preferenceManager?.putString(KEY_USER_ID, documentSnapShot.id)
                    preferenceManager?.putString(KEY_NAME, documentSnapShot.getString(KEY_NAME).toString())
                    preferenceManager?.putString(KEY_IMAGE, documentSnapShot.getString(KEY_IMAGE).toString())
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }else{
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBarSignIn.visibility = View.VISIBLE
        } else {
            binding.progressBarSignIn.visibility = View.INVISIBLE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun isValidSignInDetails(): Boolean {
        return when {
            binding.editTextEmail.text.toString().trim().isEmpty() -> {
                showToast("Enter email")
                false
            }
            (!Patterns.EMAIL_ADDRESS.matcher(binding.editTextEmail.text.toString()).matches()) -> {
                showToast("Enter valid email")
                false
            }
            binding.editTextPassword.text.toString().trim().isEmpty() -> {
                showToast("Enter password")
                false
            }
            else -> true
        }
    }

}
