package com.example.chatproject.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts

import com.example.chatproject.databinding.ActivitySignUpBinding
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_EMAIL
import com.example.chatproject.utilities.Constants.Companion.KEY_IMAGE
import com.example.chatproject.utilities.Constants.Companion.KEY_IS_SIGNED_IN
import com.example.chatproject.utilities.Constants.Companion.KEY_NAME
import com.example.chatproject.utilities.Constants.Companion.KEY_PASSWORD
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.Base64

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private var preferenceManager: PreferenceManager? = null
    private var encodedImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        preferenceManager = PreferenceManager((this))
        setListener()
    }

    private fun setListener() {
        binding.textViewSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSigUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.frameLayoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user: HashMap<String, String> = HashMap()
        user[KEY_NAME] = binding.editTextName.text.toString()
        user[KEY_EMAIL] = binding.editTextEmail.text.toString()
        user[KEY_PASSWORD] = binding.editTextPassword.text.toString()
        user[KEY_IMAGE] = encodedImage.toString()
        database.collection(KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {
                loading(false)
                preferenceManager?.putBoolean(KEY_IS_SIGNED_IN, true)
                preferenceManager?.putString(KEY_USER_ID, it.id)
                preferenceManager?.putString(KEY_NAME, binding.editTextName.text.toString())
                preferenceManager?.putString(KEY_IMAGE, binding.imageProfile.toString())
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                loading(false)
                it.message?.let { it1 -> showToast(it1) }
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap =
            Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val imageUri = it.data?.data
                try {
                    val inputSteam = imageUri?.let { it1 -> contentResolver.openInputStream(it1) }
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputSteam)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.textViewAddImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }


            }
        }


    private fun isValidSignUpDetails(): Boolean {
        if (binding.imageProfile.toString().isEmpty()) {
            showToast("Select image profile")
            return false
        }else if (binding.editTextName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.editTextEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (binding.editTextPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else if (binding.editTextConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Enter confirm password")
            return false
        } else if (binding.editTextPassword.text.toString() != binding.editTextConfirmPassword.text.toString()) {
            showToast("Password and confirm password must be same")
            return false
        }
        return true
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSigUp.visibility = View.INVISIBLE
            binding.progressBarSignUp.visibility = View.VISIBLE
        } else {
            binding.progressBarSignUp.visibility = View.INVISIBLE
            binding.buttonSigUp.visibility = View.VISIBLE
        }
    }
}