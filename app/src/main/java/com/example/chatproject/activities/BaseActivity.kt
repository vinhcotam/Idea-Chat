package com.example.chatproject.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatproject.utilities.Constants.Companion.KEY_AVAILABILITY
import com.example.chatproject.utilities.Constants.Companion.KEY_COLLECTION_USERS
import com.example.chatproject.utilities.Constants.Companion.KEY_USER_ID
import com.example.chatproject.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    private var documentReference: DocumentReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(this)
        val database = FirebaseFirestore.getInstance()
        documentReference = database.collection(KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(KEY_USER_ID).toString())

    }

    override fun onPause() {
        super.onPause()
        documentReference?.update(KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference?.update(KEY_AVAILABILITY, 1)

    }

}