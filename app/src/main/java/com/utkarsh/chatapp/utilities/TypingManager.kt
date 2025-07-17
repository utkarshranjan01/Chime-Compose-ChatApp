package com.utkarsh.chatapp.utils

import com.google.firebase.database.FirebaseDatabase

object TypingManager {

    fun setTyping(chatId: String, userId: String, isTyping: Boolean) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("typing_statuses")
            .child(chatId)
            .child(userId)

        if (isTyping) {
            ref.setValue(true)
        } else {
            ref.setValue(false)
        }
    }

    // Optionally call this on disconnect to clean up
    fun setupOnDisconnect(chatId: String, userId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("typing_statuses")
            .child(chatId)
            .child(userId)

        ref.onDisconnect().setValue(false)
    }
}

