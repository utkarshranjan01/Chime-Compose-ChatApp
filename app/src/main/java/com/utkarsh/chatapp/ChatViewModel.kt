package com.utkarsh.chatapp

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.utkarsh.chatapp.utils.PresenceManager.checkIfReceiverInSameChat
import java.io.File

class ChatViewModel : ViewModel() {


    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val userCollection = Firebase.firestore.collection(USER_COLLECTION)
    var userDataListener: ListenerRegistration? = null

    var chats by mutableStateOf<List<ChatData>>(emptyList())

    var storyListener: ListenerRegistration? = null

    var allStoriesListener: ListenerRegistration? = null

    var stories by mutableStateOf<List<Story>>(emptyList())

    var chatListener: ListenerRegistration? = null


    // These two variables to show empty image on status and chats screen and thus used to prevent flash while loading
    var isChatsInitialized by mutableStateOf(false)
    var isStoryInitialized by mutableStateOf(false)

    var tp by mutableStateOf(ChatData())

    var tpListener: ListenerRegistration? = null
    var reply by mutableStateOf("")

    private val firestore = FirebaseFirestore.getInstance() // so that don't have to write again and again Firebase.firestore

    var msgListener: ListenerRegistration? = null

    var messages by mutableStateOf<List<Message>>(listOf())

    var allMessages = mutableStateListOf<Message>()
    private var lastVisibleMessage: DocumentSnapshot? = null

    private var newMsgListener: ListenerRegistration? = null
    private var updateMsgListener: ListenerRegistration? = null

    private var isInitialLoad = false
    private var latestMessageTimestamp: Timestamp? = null

    private var reactionsListener: ListenerRegistration? = null

    var isLoadingMore by mutableStateOf(false)

//    var isTotalChats by mutableStateOf(false)

    var lastChatId by mutableStateOf<String?>(null)

    var isNavigating by mutableStateOf(false)

    var currentImageMessage by mutableStateOf<Message?>(null)

    // For searching the users in all chats screen
    var searchQuery by mutableStateOf("")

//    var hasReachedStartOfChat by mutableStateOf(false)

    val chatMessagesPreloaded = mutableStateMapOf<String, SnapshotStateList<Message>>()

    val chatMessageCompleteLoaded = mutableStateMapOf<String, Boolean>()

    val userMetadataCache = mutableStateMapOf<String, Triple<String, String, String>>()

    val lastReadTimestamp = mutableStateOf(Timestamp(0, 0))

    val userPresenceMap = mutableStateMapOf<String, Pair<Boolean, Long?>>()

    val messageVersion = mutableIntStateOf(0)

//    var typingUsers = mutableStateOf(emptyList<String>())


//    var isLoadingMore = false


//    var hasLoadedAllMessages by mutableStateOf(false)

    var hasLoadedAllMessages = false

//    var hasLoadedInitialMessages by mutableStateOf(false)

    var hasLoadedInitialMessages = false

    fun updateUserProfile(username: String, bio: String) {
        val userDataMap = mapOf(
            "username" to username,
            "bio" to bio,
        )

        val userId = state.value.userData?.userId ?: return
        userCollection.document(userId).update(userDataMap)
            .addOnSuccessListener {
                _state.update { currentState ->
                    currentState.copy(
                        userData = currentState.userData?.copy(
                            username = username,
                            bio = bio,
                        )
                    )
                }
//                setSetupComplete()
            }
            .addOnFailureListener {
                Log.e(TAG, "Error updating user profile", it)
            }
    }

    fun resetState() {

        _state.update {
            AppState() // Reset to initial state
        }
        messages = emptyList()
        chats = emptyList()
        userDataListener?.remove()
        chatListener?.remove()
        tpListener?.remove()
        msgListener?.remove()
        storyListener?.remove()
        newMsgListener?.remove()
        updateMsgListener?.remove()
        reactionsListener?.remove()
    }

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn = _isSigningIn.asStateFlow()

    fun setSigningIn(signing: Boolean) {
        _isSigningIn.value = signing
    }

    fun onSignInResult(signInResult: SignInResult) {
        _state.update {
            it.copy(
                isSignedIn = signInResult.data != null,
                signInError = signInResult.errorMessage
            )
        }
    }

//    fun onSignInResult(signInResult: SignInResult) {
//        // this runs after the user has signed in
//
//        _state.update {
//            it.copy(
//                isSignedIn = signInResult.data != null,
//                signInError = signInResult.errorMessage,
//            )
//        }
//
//    }

    fun adduserToFirestore(userData: UserData?) {
        val userDataMap = mapOf(
            "userId" to userData?.userId,
            "username" to userData?.username,
            "ppurl" to userData?.ppurl,
            "email" to userData?.email,

            )
        val userDocument = userCollection.document(userData?.userId ?: "")
        userDocument.get().addOnSuccessListener {
            if (it.exists()) {
                // User document already exists, update it so basically if the username or ppurl or other changes happen

                userDocument.update(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data updated to Firestore successfully")
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Error updating user data to Firestore")
                }
            } else {
                // User does not exist add it to database

                userDocument.set(userDataMap).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data added to Firestore successfully")
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Error adding user data to Firestore")
                }

            }
        }

    }

    fun getUserData(userId: String) {
        userDataListener = userCollection.document(userId).addSnapshotListener { value, error ->
            if (value != null) {
                _state.update {
                    it.copy(userData = value.toObject(UserData::class.java))
                }
            }
        }
    }

    fun hideDialog() {
        _state.update {
            it.copy(showDialog = false)
        }
    }

    fun showDialog() {
        _state.update {
            it.copy(showDialog = true)
        }
    }

    fun setSrEmail(email: String) {
        _state.update {
            it.copy(srEmail = email)
        }
    }

    fun addChat(email: String, context: Context) {
        Firebase.firestore.collection(CHAT_COLLECTION).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.email", email),
                    Filter.equalTo("user2.email", state.value.userData?.email),
                ),
                Filter.and(
                    Filter.equalTo("user1.email", state.value.userData?.email),
                    Filter.equalTo("user2.email", email),
                )
            )
        ).get().addOnSuccessListener {
            if (it.isEmpty) { // so that we don't add the same user again

                userCollection.whereEqualTo("email", email).get().addOnSuccessListener {

                    if (it.isEmpty) {
                        Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    } else {

                        val chatPartner = it.toObjects(UserData::class.java).firstOrNull()

                        val id = Firebase.firestore.collection(CHAT_COLLECTION).document().id
                        val chat = ChatData(
                            chatId = id,
                            last = Message(
                                senderId = "",
                                content = "",
                                time = null,
                            ),
                            user1 = ChatUserData(
                                userId = state.value.userData?.userId.toString(),
                                typing = false,
                                bio = state.value.userData?.bio.toString(),
                                username = state.value.userData?.username.toString(),
                                ppurl = state.value.userData?.ppurl.toString(),
                                email = state.value.userData?.email.toString(),
                            ),
                            user2 = ChatUserData(
                                userId = chatPartner?.userId.toString(),
                                typing = false,
                                bio = chatPartner?.username.toString(),
                                username = chatPartner?.username.toString(),
                                ppurl = chatPartner?.ppurl.toString(),
                                email = chatPartner?.email.toString(),
                            )
                        )

                        Firebase.firestore.collection(CHAT_COLLECTION).document(id).set(chat)
                        Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Error fetching user info", Toast.LENGTH_SHORT).show()
                }
            }
            else Toast.makeText(context, "User already added search", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error: User Not Added", Toast.LENGTH_SHORT).show()
        }
    }

    fun showChats(userId: String) {
        chatListener = Firebase.firestore.collection(CHAT_COLLECTION).where(
            Filter.or(
                Filter.equalTo("user1.userId", userId),
                Filter.equalTo("user2.userId", userId)
            )
        ).addSnapshotListener { value, error ->
            if (value != null) {
                chats = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }.sortedBy {
                    it.last?.time
                }.reversed()
            }
            isChatsInitialized = true
        }
    }

    fun getTp(chatId: String) {
        tpListener?.remove() // it removes the previous listener for other chat so it gets fresh data

        tpListener = Firebase.firestore.collection(CHAT_COLLECTION).document(chatId)
            .addSnapshotListener { snp, err ->
                if (snp != null) {
                    tp = snp.toObject(ChatData::class.java)!!
                }
            }
    }

    // This sets the second user of the chat
    fun setChatUser(usr: ChatUserData, id: String) {
        _state.update {
            it.copy(
                User2 = usr, chatId = id
            )
        }
    }

    fun loadInitialMessages2(chatId: String, pageSize: Long = 20) {

        val allMessages2 = mutableStateListOf<Message>()

        firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .collection(MESSAGE_COLLECTION)
            .orderBy("time", Query.Direction.DESCENDING)
            .limit(pageSize)
            .get()
            .addOnSuccessListener { snapshot ->
                val msgs = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                allMessages2.addAll(msgs)

//                chatMessagesPreloaded[chatId] = allMessages2

//                val currentMessages = chatMessagesPreloaded[chatId]?.toMutableList() ?: mutableListOf()
//                currentMessages.addAll(msgs)
//                chatMessagesPreloaded[chatId] = currentMessages
                chatMessagesPreloaded[chatId] = allMessages2
                if(allMessages2.size < pageSize)
                {
                    chatMessageCompleteLoaded[chatId]=true
                }
                else
                {
                    chatMessageCompleteLoaded[chatId] = false
                }
            }
    }

    fun loadMoreMessages(chatId: String, pageSize: Long = 20) {
//        println("loadMoreMessages called - isLoadingMore: $isLoadingMore, lastVisibleMessage: $lastVisibleMessage, isInitialLoad: $isInitialLoad")

        val currentMessagesList = chatMessagesPreloaded[chatId]?: mutableStateListOf()

//        if (isLoadingMore || lastVisibleMessage == null || isInitialLoad || hasLoadedAllMessages) return
        if(isLoadingMore || currentMessagesList.isEmpty() || chatMessageCompleteLoaded[chatId] == true) return
        isLoadingMore = true

        val oldestMessageTimestamp = currentMessagesList.lastOrNull()?.time

//        println("Loading more messages for chat: $chatId")

        firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .collection(MESSAGE_COLLECTION)
            .orderBy("time", Query.Direction.DESCENDING)
            .startAfter(oldestMessageTimestamp!!)
            .limit(pageSize)
            .get()
            .addOnSuccessListener { snapshot ->
                val msgs = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }

                val chatMessages = chatMessagesPreloaded.getOrPut(chatId) { mutableStateListOf() }

                // Filter out duplicates before adding
                val newMessages = msgs.filter { newMsg ->
                    !chatMessages.any { existingMsg -> existingMsg.msgId == newMsg.msgId }
                }
                if (newMessages.isEmpty()) {
//                    hasLoadedAllMessages = true
                    chatMessageCompleteLoaded[chatId]=true
                }
                else
                {
                    chatMessages.addAll(newMessages)
                    if(newMessages.size < pageSize)
                    {
                        chatMessageCompleteLoaded[chatId] = true
                    }
                }
//                allMessages.addAll(newMessages)

//                if (snapshot.documents.isNotEmpty()) {
//                    lastVisibleMessage = snapshot.documents.last()
//                }

                isLoadingMore = false
            }
            .addOnFailureListener { exception ->
//                println("Error loading more messages: ${exception.message}")
                isLoadingMore = false
            }
    }


    fun listenForNewMessages(chatId: String) {

        newMsgListener?.remove()

        // Get the current list of messages for this chat ID
        val currentMessagesList = chatMessagesPreloaded[chatId]?: mutableStateListOf()

        // Determine the latest timestamp from the currently loaded messages
        val latestTimestamp = currentMessagesList.firstOrNull()?.time
//        val latestTimestamp2 = currentMessagesList.firstOrNull()?.time
//        Log.d("ChatViewModel", "Latest timestamp: $latestTimestamp $latestTimestamp2")

        val query = if (latestTimestamp != null) {
            firestore.collection(CHAT_COLLECTION)
                .document(chatId)
                .collection(MESSAGE_COLLECTION)
                .orderBy("time", Query.Direction.DESCENDING)
                .whereGreaterThan("time", latestTimestamp)
        } else {
            firestore.collection(CHAT_COLLECTION)
                .document(chatId)
                .collection(MESSAGE_COLLECTION)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(1)
        }

        newMsgListener = query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val newMessages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }

                val chatMessages = chatMessagesPreloaded.getOrPut(chatId) { mutableStateListOf() }

                // Filter out messages that already exist in allMessages
                val uniqueNewMessages = newMessages.filter { newMsg ->
                    !chatMessages.any { existingMsg -> existingMsg.msgId == newMsg.msgId }
                }

                if (uniqueNewMessages.isNotEmpty()) {
//                    chatMessages.addAll(uniqueNewMessages.sortedByDescending { it.time })
//                    chatMessages.addAll(uniqueNewMessages)
                    chatMessages.addAll(0, uniqueNewMessages)
                }
            }
        }

    }

    fun listenForMessageUpdates(chatId: String) {
        updateMsgListener?.remove()

        updateMsgListener = firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .collection(MESSAGE_COLLECTION)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val updatedMessages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    val chatMessages = chatMessagesPreloaded.getOrPut(chatId) { mutableStateListOf() }

                    updatedMessages.forEach { updated ->
                        val index = chatMessages.indexOfFirst { it.msgId == updated.msgId }
                        if (index != -1) {
                            chatMessages[index] = updated // triggers recomposition
                        }
                    }
                    messageVersion.intValue = messageVersion.intValue + 1
                }
            }
    }





    fun closeMessageListener() {
        newMsgListener?.remove()
        newMsgListener = null

        updateMsgListener?.remove()
        updateMsgListener = null

        tpListener?.remove() // Close the chat listener
    }


    // Send the text message
    fun sendReply(
        chatId: String, replyMessage: Message = Message(), msg: String,
        senderId: String = state.value.userData?.userId.toString(),
        receiverId: String = state.value.User2?.userId.toString(),
        senderName: String = state.value.userData?.username.toString(),
        ppurl: String = state.value.userData?.ppurl.toString(),
        user1: String = tp.user1?.userId.toString(),
        user2: String = tp.user2?.userId.toString(),
    ) {
        val id = Firebase.firestore.collection(CHAT_COLLECTION).document().collection(MESSAGE_COLLECTION).document().id

        val time = Calendar.getInstance().time
        val timestamp = Timestamp(date = time)

        val message = Message(
            msgId = id,
            repliedMessage = replyMessage,
            senderId = senderId,
            content = msg.trimEnd(),
            time = timestamp,
            receiverId = receiverId,
            senderName = senderName,
            text = msg.trimEnd(),
            ppurl = ppurl
        )

        Firebase.firestore.collection(CHAT_COLLECTION).document(chatId).collection(
            MESSAGE_COLLECTION
        ).document(id).set(message)

        // Update the last message to show in the home screen
        firestore.collection(CHAT_COLLECTION).document(chatId).update("last", message)

//        val isReceiverOnline = userPresenceMap[receiverId]?.first == true

        // Update the unread messages number to show in the home screen
        checkIfReceiverInSameChat(receiverId, chatId) { isInSameChat ->
            if (!isInSameChat) {
                val unreadField = if (senderId == user1) "user2.unread" else "user1.unread"
                firestore.collection(CHAT_COLLECTION)
                    .document(chatId)
                    .update(unreadField, FieldValue.increment(1))
            }
        }

        // Update latest timestamp for new message detection
        latestMessageTimestamp = timestamp
    }

    fun sendMediaMessage(
        file: Uri,
        chatId: String,
        context: Context
    ) {
        val id = Firebase.firestore.collection(CHAT_COLLECTION)
            .document().collection(MESSAGE_COLLECTION).document().id
        val timestamp = Timestamp.now()

        // Detect file type from URI
        val mimeType = context.contentResolver.getType(file)
        val isImage = mimeType?.startsWith("image/") == true
        val isVideo = mimeType?.startsWith("video/") == true
        val isFile = !isImage && !isVideo

        val message = Message(
            msgId = id,
            senderId = state.value.userData?.userId.orEmpty(),
            receiverId = state.value.User2?.userId.orEmpty(),
            senderName = state.value.userData?.username.orEmpty(),
            time = timestamp,
            ppurl = state.value.userData?.ppurl.orEmpty(),
            imageUrl = if (isImage) "temp" else "",
            vidUrl = if (isVideo) "temp" else "",
            fileUrl = if (isFile) "temp" else "",
            content = if (isFile) {
                getFileName(context, file)
            } else ""
        )

        // Create placeholder message in Firestore
        Firebase.firestore.collection(CHAT_COLLECTION).document(chatId)
            .collection(MESSAGE_COLLECTION).document(id).set(message)

        Firebase.firestore.collection(CHAT_COLLECTION).document(chatId)
            .update("last", message)

        uploadFile(file, context) { url ->
            if (url.isNotBlank()) {
                val (updatedMessage, updateField) = when {
                    isImage -> message.copy(imageUrl = url) to "imageUrl"
                    isVideo -> message.copy(vidUrl = url) to "vidUrl"
                    else -> message.copy(fileUrl = url) to "fileUrl"
                }

                Firebase.firestore.collection(CHAT_COLLECTION).document(chatId)
                    .collection(MESSAGE_COLLECTION).document(id).update(updateField, url)

                Firebase.firestore.collection(CHAT_COLLECTION).document(chatId)
                    .update("last.$updateField", url)

                checkIfReceiverInSameChat(updatedMessage.receiverId, chatId) { inSameChat ->
                    if (!inSameChat) {
                        val unreadField = if (updatedMessage.senderId == tp.user1?.userId) "user2.unread" else "user1.unread"
                        Firebase.firestore.collection(CHAT_COLLECTION)
                            .document(chatId)
                            .update(unreadField, FieldValue.increment(1))
                    }
                }

                latestMessageTimestamp = timestamp
            }
        }
    }

    fun updateMessageReaction(message: Message, emoji: String) {

        val field = if(message.senderId == state.value.userData?.userId) "reactSender" else "reactReceiver"

        Firebase.firestore.collection(CHAT_COLLECTION).document(tp.chatId).collection(MESSAGE_COLLECTION)
            .document(message.msgId).update(field, emoji)

        if(message.msgId == (tp.last?.msgId ?: ""))
        {
            Firebase.firestore.collection(CHAT_COLLECTION).document(tp.chatId).update("last.$field", emoji)
        }

    }

    fun uploadFile(file: Uri, context: Context, callback: (String) -> Unit) {

        val mimeType = context.contentResolver.getType(file)

        val whichCollection = when{
            mimeType?.startsWith("image/") == true -> IMAGE_COLLECTION
            mimeType?.startsWith("video/") == true -> VIDEO_COLLECTION
            else -> FILE_COLLECTION
        }
        val storageRef = Firebase.storage.reference
        val fileRef = storageRef.child("$whichCollection/${System.currentTimeMillis()}")

        fileRef.putFile(file).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener {
                val url = it.toString()
                callback(url)
            }
        }.addOnFailureListener {
            callback("")
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "Untitled File"

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        } else if (uri.scheme == "file") {
            name = File(uri.path ?: "").name
        }

        return name
    }

    fun monitorDeletedMessages(chatId: String) {
        val cachedList = chatMessagesPreloaded[chatId] ?: return

        Firebase.firestore.collection(CHAT_COLLECTION)
            .document(chatId)
            .collection(MESSAGE_COLLECTION)
            .addSnapshotListener { snapshot, _ ->
                val currentIdsInFirebase = snapshot?.documents?.map { it.id }?.toSet() ?: return@addSnapshotListener

                val toRemove = cachedList.filter { it.msgId !in currentIdsInFirebase }

                if (toRemove.isNotEmpty()) {
                    cachedList.removeAll(toRemove.toSet())
                }
            }
    }

    fun uploadStory(url: String, storyId: String) {
        val image = Image(
            imgUrl = url,
            time = Timestamp(Calendar.getInstance().time)
        )
        if (storyId.isNotBlank()) // basically blank is checking if it contains any character other than whitespace
        {
            // this to add multiple stories
            firestore.collection(STORIES_COLLECTION).document(storyId).update("images", FieldValue.arrayUnion(image))
        }
        else
        {
            val id = firestore.collection(STORIES_COLLECTION).document().id
            val story = Story(
                id = id,
                userId = state.value.userData?.userId.toString(),
                userName = state.value.userData?.username,
                ppurl = state.value.userData?.ppurl.toString(),
                images = listOf(image)
            )
            firestore.collection(STORIES_COLLECTION).document(id).set(story)
        }
    }

    fun deleteChat(chatId: String)
    {
        FirebaseFirestore.getInstance().collection(CHAT_COLLECTION)
            .document(chatId)
            .collection("messages") // or whatever your subcollection is
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach {
                    batch.delete(it.reference)
                }
                batch.commit().addOnSuccessListener {
                    // finally delete the parent doc
                    firestore.collection(CHAT_COLLECTION)
                        .document(chatId)
                        .delete()
                        .addOnSuccessListener {
                            chatMessagesPreloaded.remove(chatId)
                            popStory(state.value.userData?.userId?:"") // Called after deleting the selected chats collection from the database
                        }
                }
            }
    }

    fun updateProfile(url: String, userId: String, context: Context) {
        if (userId.isNotBlank())
        {
            firestore.collection(USER_COLLECTION).document(userId).update("ppurl",url)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update profile picture.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Failed to update profile picture.", Toast.LENGTH_SHORT).show()
        }
    }

    fun observeUserPresence(userId: String) {
        Log.d("ChatUI66", "Checking")
        val ref = FirebaseDatabase.getInstance().getReference("status/$userId")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("state").getValue(String::class.java) == "online"
                val lastSeen = snapshot.child("last_changed").getValue(Long::class.java)
                userPresenceMap[userId] = Pair(isOnline, lastSeen)
                Log.d("ChatUI66", "Presence: $isOnline $lastSeen")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

//    suspend fun fetchUserMetadata(userId: String) {
    fun fetchUserMetadata(userId: String) {
        if (userMetadataCache.containsKey(userId)) return // already cached

        Firebase.firestore.collection("users")
            .document(userId)
            .addSnapshotListener { docSnapshot, error ->
                if (error != null || docSnapshot == null || !docSnapshot.exists()) return@addSnapshotListener

                val username = docSnapshot.getString("username") ?: "Unknown"
                val bio = docSnapshot.getString("bio") ?: ""
                val ppUrl = docSnapshot.getString("ppurl") ?: ""

                userMetadataCache[userId] = Triple(username, bio, ppUrl)
            }

//        try {
//            val doc =  Firebase.firestore.collection("users").document(userId).get().await()
//            val username = doc.getString("username") ?: "Unknown"
//            val bio = doc.getString("bio") ?: ""
//            val ppUrl = doc.getString("ppurl") ?: ""
//
//            userMetadataCache[userId] = Triple(username, bio, ppUrl)
//
//        } catch (e: Exception) {
//        }
    }

    fun deleteStoryImage(storyId: String, image: Image) {

        stories.find { it.id == storyId }?.images?.size?.let {
            if (it > 1) {
                firestore.collection(STORIES_COLLECTION)
                    .document(storyId)
                    .update("images", FieldValue.arrayRemove(image))
                    .addOnSuccessListener {
                        stories = stories.map { story ->
                            if (story.id == storyId) {
                                story.copy(images = story.images.filter { it != image })
                            } else story
                        }
                    }
            } else {
                firestore.collection(STORIES_COLLECTION)
                    .document(storyId)
                    .delete()
                    .addOnSuccessListener {
                        stories = stories.filter { it.id != storyId }
                    }
            }
        }
    }


    fun popStory(currentUserId: String) {
//        viewModelScope.launch { // not really needed as we are doing a listener and removing later

            allStoriesListener?.remove()
            storyListener?.remove()


            val storyCollection = firestore.collection(STORIES_COLLECTION)
            val users = arrayListOf(state.value.userData?.userId) // to create a list of user ids

            allStoriesListener = firestore.collection(CHAT_COLLECTION).where(
                Filter.or(
                    Filter.equalTo("user1.userId", currentUserId),
                    Filter.equalTo("user2.userId", currentUserId)
                )

            ).addSnapshotListener { snp, err ->

                if (snp != null) {
                    snp.toObjects<ChatData>().forEach {
                        val otherUserId = if (it.user1?.userId == currentUserId) {
                            it.user2?.userId.toString()
                        } else {
                            it.user1?.userId.toString()
                        }

                        users.add(otherUserId)
                    }
                    users.add(currentUserId)

                    storyListener = storyCollection.whereIn("userId", users)
                        .addSnapshotListener { storySnapShot, storyError ->

                            if (storySnapShot != null) {
                                stories = storySnapShot.documents.mapNotNull {
                                    it.toObject<Story>()
                                }
                            }

                        }
                }
                isStoryInitialized = true
            }
//        }

    }
}
