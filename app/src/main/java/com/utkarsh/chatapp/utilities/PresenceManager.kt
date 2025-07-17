package com.utkarsh.chatapp.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

object PresenceManager {

    fun setOnline(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("status/$userId")

        val onlineStatus = mapOf(
            "state" to "online",
            "last_changed" to ServerValue.TIMESTAMP,
        )

        // Set fallback on app disconnect (crash, quit, no internet)
        ref.onDisconnect().setValue(
            mapOf(
                "state" to "offline",
                "last_changed" to ServerValue.TIMESTAMP
            )
        )

        // Update status immediately
        ref.setValue(onlineStatus)
    }

    fun setOffline(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("status/$userId")
        val offlineStatus = mapOf(
            "state" to "offline",
            "last_changed" to ServerValue.TIMESTAMP
        )

        ref.setValue(offlineStatus)
    }

    fun setCurrentChat(userId: String, chatId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("status/$userId/current_chat")
        ref.setValue(chatId)
    }

    fun clearCurrentChat(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("status/$userId/current_chat")
        ref.setValue("")
    }

    fun checkIfReceiverInSameChat(receiverId: String, chatId: String, onResult: (Boolean) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("status/$receiverId/current_chat")

        ref.get().addOnSuccessListener { snapshot ->
            val currentChat = snapshot.getValue(String::class.java)
            onResult(currentChat == chatId)
        }.addOnFailureListener {
            // Fallback in case of error
            onResult(false)
        }
    }


    fun observeTypingStatus(
        chatId: String,
        currentUserId: String,
        onTypingUsersChanged: (List<String>) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("typing_statuses")
            .child(chatId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingUsers = mutableListOf<String>()
                for (child in snapshot.children) {
                    val userId = child.key ?: continue
                    val isTyping = child.getValue(Boolean::class.java) ?: false

                    if (isTyping && userId != currentUserId) {
                        typingUsers.add(userId)
                    }
                }
                onTypingUsersChanged(typingUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatUI12", "Error $chatId and $currentUserId", error.toException())
                Log.e("TypingObserver", "Typing status listen failed", error.toException())
            }
        })
    }

    @Composable
    fun TrackPresence(userId: String) {
        val lifecycleOwner = rememberUpdatedState(newValue = ProcessLifecycleOwner.get())

        DisposableEffect(userId) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        PresenceManager.setOnline(userId)
                    }

                    Lifecycle.Event.ON_STOP -> {
                        PresenceManager.setOffline(userId)
                    }

                    else -> {}
                }
            }

            val lifecycle = lifecycleOwner.value.lifecycle
            lifecycle.addObserver(observer)

            onDispose {
                lifecycle.removeObserver(observer)
            }
        }
    }

}

