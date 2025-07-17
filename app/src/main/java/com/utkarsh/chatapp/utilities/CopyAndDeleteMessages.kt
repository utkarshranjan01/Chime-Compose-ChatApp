package com.utkarsh.chatapp.utilities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.utkarsh.chatapp.AppState
import com.utkarsh.chatapp.CHAT_COLLECTION
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.MESSAGE_COLLECTION
import com.utkarsh.chatapp.Message

fun copySelectedMessagesToClipboard(
    selectedMessages: Map<String, Message>,
    context: Context
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val combinedText = selectedMessages.values
        .sortedBy { it.time } // This way it will keep its order
        .joinToString("\n") { it.text }

    clipboard.setPrimaryClip(ClipData.newPlainText("messages", combinedText))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

// Delete the selected messages inside a chat
fun deleteSelectedMessages(
    selectedMessages: Map<String, Message>,
    viewModel: ChatViewModel,
    chatId: String,
    state: AppState
) {
    val firestore = Firebase.firestore

    // Changing the unread count of other user
    val unReadCount = selectedMessages.values.count { !it.read }
    if(unReadCount>0)
    {
        var otherUserCount = if (state.userData?.userId == viewModel.tp.user1?.userId) viewModel.tp.user2?.unread?:0 else viewModel.tp.user1?.unread?:0

        otherUserCount = otherUserCount-unReadCount
        if(otherUserCount<0) otherUserCount=0

        val otherUser = if (state.userData?.userId == viewModel.tp.user1?.userId) "user2.unread" else "user1.unread"
        Firebase.firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .update(otherUser, otherUserCount)
    }

    selectedMessages.values.forEach { msg ->
        firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .collection(MESSAGE_COLLECTION)
            .document(msg.msgId)
            .delete()

        viewModel.chatMessagesPreloaded[chatId]?.removeIf { it.msgId == msg.msgId }

        // After deletions, update the 'last' message in the chat document
        val newLastMessage = viewModel.chatMessagesPreloaded[chatId]?.firstOrNull()
        firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .update("last", newLastMessage)
    }
}
