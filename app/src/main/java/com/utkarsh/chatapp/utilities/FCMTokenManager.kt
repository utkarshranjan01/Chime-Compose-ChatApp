package com.utkarsh.chatapp.utilities

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FCMTokenManager {

    fun uploadToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCMToken", "Token updated successfully")
                    }
                    .addOnFailureListener {
                        Log.e("FCMToken", "Failed to update token", it)
                    }
            } else {
                Log.w("FCMToken", "User not logged in")
            }
        }.addOnFailureListener {
            Log.e("FCMToken", "Failed to get FCM token", it)
        }
    }
}