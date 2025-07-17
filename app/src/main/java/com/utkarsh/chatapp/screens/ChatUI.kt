package com.utkarsh.chatapp.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.utkarsh.chatapp.AppState
import com.utkarsh.chatapp.ChatUserData
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.Message
import com.utkarsh.chatapp.R
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import coil3.video.videoFrameMillis
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.firebase.firestore.FirebaseFirestore
import com.utkarsh.chatapp.CHAT_COLLECTION
import com.utkarsh.chatapp.ImageScreen
import com.utkarsh.chatapp.MESSAGE_COLLECTION
import com.utkarsh.chatapp.utilities.TimeDisplay.LastSeenText
import com.utkarsh.chatapp.utilities.TimeDisplay.formatDate
import com.utkarsh.chatapp.utilities.TimeDisplay.toLocalDateOnly
import com.utkarsh.chatapp.utilities.copySelectedMessagesToClipboard
import com.utkarsh.chatapp.utilities.deleteSelectedMessages
import com.utkarsh.chatapp.utilities.downloadFileFromMessage
import com.utkarsh.chatapp.VideoScreen
import com.utkarsh.chatapp.components.ReactionTab
import com.utkarsh.chatapp.dialogs.DeleteConfirmationDialog
import com.utkarsh.chatapp.dialogs.DownloadDialog
import com.utkarsh.chatapp.utils.PresenceManager.clearCurrentChat
import com.utkarsh.chatapp.utils.PresenceManager.observeTypingStatus
import com.utkarsh.chatapp.utils.PresenceManager.setCurrentChat
import com.utkarsh.chatapp.utils.TypingManager
import com.utkarsh.chatapp.utils.TypingManager.setupOnDisconnect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

//@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatUI(
    viewModel: ChatViewModel,
    userData: ChatUserData,
    chatId: String="",
    state: AppState,
    onBack: () -> Unit={},
    context: Context = LocalContext.current,
    navController: NavController,
) {

    val lazyListState = rememberLazyListState()

    val tp = viewModel.tp

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

//    viewModel.messageVersion.intValue = 0

    val coroutineScope = rememberCoroutineScope()

//    val messages = viewModel.allMessages

    val emptySnapshotList = remember { mutableStateListOf<Message>() }

    val haptic = LocalHapticFeedback.current

    val messages by remember(state.chatId, viewModel.chatMessagesPreloaded) {
        derivedStateOf {
            viewModel.chatMessagesPreloaded[state.chatId] ?: emptySnapshotList
        }
    }
    val presence by remember { derivedStateOf { viewModel.userPresenceMap[state.User2?.userId] } }

    var selectedMessages by remember { mutableStateOf<Map<String, Message>>(emptyMap()) }

    var hasClearedUnreadMessages by remember { mutableStateOf(false) }

    var showReactions by remember { mutableStateOf(false) }

    var showReactionsId = ""

    var reactionOffset by remember { mutableStateOf(Offset.Zero) }

    var selectedReactionMessage by remember { mutableIntStateOf(0) }  // this will store index of long press message

    // So that first back gesture removes any selected messages then can normally
    BackHandler {
        if (selectedMessages.isNotEmpty()) {
            selectedMessages = emptyMap()
            showReactions = false
            showReactionsId = ""
        } else {
            onBack()
        }
    }


    // Unread message count
    val unreadCountAtEntry = remember(state.chatId) { mutableIntStateOf(0) }

    // storing the message id of the first unread message to display indicator before it
    val unreadMarkerMessageId = remember(state.chatId) { mutableStateOf<String?>(null) }

    // related to unread messages
    LaunchedEffect(state.chatId)
    {
        // get the unread messages count before clearing it
        unreadCountAtEntry.value = if (state.userData?.userId == tp.user1?.userId) { tp.user1?.unread ?: 0 } else { tp.user2?.unread ?: 0 }

        // This is to make unread messages 0 when entering a chat of the current user
        val unreadField = if (state.userData?.userId == tp.user1?.userId) "user1.unread" else "user2.unread"
        FirebaseFirestore.getInstance().collection(CHAT_COLLECTION)
            .document(chatId)
            .update(unreadField,0)

        hasClearedUnreadMessages = true
    }


    // THIS CONDITION GUARANTEES THAT ALL THE MESSAGES ARE LOADED AS WE PRELOAD MESSAGES ON FIRST LOADING APP
    // SO ANY NEW MESSAGES NEED TO BE LOADED AFTER OPENING THE CHAT  tp.last?.msgId == messages.firstOrNull()?.msgId


    // For setting the right position of unread line before the right msg storing its message id and then putting if before it
    LaunchedEffect(messages.size) {
        if(unreadMarkerMessageId.value == null)
        {
            if(tp.last?.msgId == messages.firstOrNull()?.msgId)
            {
                val targetIndex = unreadCountAtEntry.value - 1
                if (targetIndex >= 0 && targetIndex < messages.size && unreadMarkerMessageId.value == null) {
                    unreadMarkerMessageId.value = messages[targetIndex].msgId
                }
            }
        }
    }

    // For loading messages and listening for new messages and listening for reactions
    LaunchedEffect(state.chatId) {
        viewModel.listenForNewMessages(state.chatId)
        viewModel.listenForMessageUpdates(state.chatId)
//        viewModel.listenForNewReactions(state.chatId)

        // This is for the typing indicator to turn off when some disconnect
        setupOnDisconnect(state.chatId, state.userData?.userId ?:"")

        viewModel.lastChatId=state.chatId
        if ( state.chatId.isNotEmpty() && (viewModel.chatMessagesPreloaded[state.chatId].isNullOrEmpty()) ) {
            viewModel.loadInitialMessages2(state.chatId)
        }
    }


    val hasScrolledToUnread = remember(state.chatId) { mutableStateOf(false) }


    // Scrolling to the unread message and also managing the selection and reaction of deleted message like if you selected and other person deleted
    LaunchedEffect(messages.size) {

        selectedMessages = selectedMessages.filterValues { selectedMsg ->
            messages.any { msg -> msg.msgId == selectedMsg.msgId }
        }

        if(showReactions)
        {
            if(messages.find { it.msgId == showReactionsId } == null)
            {
                showReactions = false
                showReactionsId = ""
            }
        }


        if (!hasScrolledToUnread.value && tp.last?.msgId == messages.firstOrNull()?.msgId) {
            val targetIndex = unreadCountAtEntry.intValue - 1
            if (targetIndex in messages.indices) { // checks if the target index is not bigger that the messages stored

                lazyListState.scrollToItem(targetIndex) // As when we preload it scrolls to the bottom
                // and if we get new messages initially then its not at the bottom

                // Wait for layout to be ready to scroll it checks if out of visible items is index 0 there
                snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
                    .first { list -> list.any { it.index == targetIndex } }


                val layoutInfo = lazyListState.layoutInfo
                val layoutItemCount = lazyListState.layoutInfo.totalItemsCount
                val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
                val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

                var itemHeight = targetItem?.size ?: 0

                var offset = 0

                // Using reverse layout in Lazy column so always the bottom of the item with stick to bottom of the lazy column so setting height accordingly

                if(messages.size == 1) offset= itemHeight
                else if(itemHeight > (viewportHeight/2)) offset=itemHeight-(viewportHeight/2) - 100
                else if(itemHeight > (viewportHeight/3)) offset=-(viewportHeight/3) - 50
                else offset=-(viewportHeight/2) - 50

                lazyListState.scrollToItem(targetIndex, offset)
                hasScrolledToUnread.value = true
            }
            else if((unreadCountAtEntry.intValue)==0) hasScrolledToUnread.value = true
        }
    }


    // This is used as disposable might not be fast enough sometimes so if we click back and quickly click another chat it might not allow
    LaunchedEffect(state.chatId) {
        delay(500)
        viewModel.isNavigating = false
    }

    // closes different listeners, typing status, current chat id when we leave this a chat screen
    DisposableEffect(state.chatId) {
        onDispose {

            clearCurrentChat(state.userData?.userId.toString())

            viewModel.closeMessageListener()

//            viewModel.isNavigating = false
            // Turning all users as off when exiting chat
            TypingManager.setTyping(state.chatId, state.userData?.userId.toString(), false)
        }
    }


    // This is to monitor deleted messages in the preload messages list
    LaunchedEffect(state.chatId) {
        viewModel.monitorDeletedMessages(chatId)
    }

    // This is to check if unread has more messages than current preloaded
    LaunchedEffect(state.chatId) {

        var curUserUnread = if(state.userData?.userId == tp.user1?.userId) tp.user1?.unread?:0 else tp.user2?.unread?:0
        curUserUnread = curUserUnread + 5

        if(curUserUnread > messages.size)
        {
            viewModel.loadMoreMessages(state.chatId, (curUserUnread-messages.size).toLong())
            lazyListState.scrollToItem(curUserUnread)
        }
    }


    // triggering when to load more messages
    LaunchedEffect(state.chatId) {
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (visibleItemsInfo.isNotEmpty()) {
                val lastVisibleItemIndex = visibleItemsInfo.last().index
                lastVisibleItemIndex >= messages.size - 10 && messages.isNotEmpty()
            // basically when we are in last 10 messages it starts loading for new messages
            } else {
                false
            }
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !viewModel.isLoadingMore) {
                viewModel.loadMoreMessages(state.chatId)
            }
        }
    }


    // For Updating the last read timestamp and also updating the unread messages
    LaunchedEffect(messages.size, viewModel.messageVersion.intValue) {

        viewModel.lastReadTimestamp.value = com.google.firebase.Timestamp(0,0)

//        if (messages.isNotEmpty()) {
//            val lastMessage = messages.lastOrNull()
//            if (lastMessage?.senderId != state.userData?.userId) {
//                // Update last message read status
//                FirebaseFirestore.getInstance().collection(CHAT_COLLECTION)
//                    .document(chatId)
//                    .update("last.read", true)
//            }

            // Update unread messages - batch update

            val unreadMessages = messages.filter {
                it.senderId != state.userData?.userId && !it.read
            }

            if (unreadMessages.isNotEmpty()) {
                val batch = FirebaseFirestore.getInstance().batch()
                unreadMessages.forEach { message ->
                    val docRef = FirebaseFirestore.getInstance()
                        .collection(CHAT_COLLECTION)
                        .document(chatId)
                        .collection(MESSAGE_COLLECTION)
                        .document(message.msgId)
                    batch.update(docRef, "read", true)
                }
                batch.commit()
            }
            val readMessages = messages.filter {
                it.senderId == state.userData?.userId && it.read
            }
//            Log.d("ChatUIInfo55", "${readMessages.size} ${unreadMessages.size} ${messages.size}")

            val latestReadMsg = readMessages.firstOrNull()
//            Log.d("ChatUIInfo", "Latest read message: ${latestReadMsg?.time}")
            if (latestReadMsg?.time != null) {
                viewModel.lastReadTimestamp.value = latestReadMsg.time
//                Log.d("ChatUIInfo5", "Updated")
            }
//            else
//            {
//                Log.d("ChatUIInfo5", "Not Updated")
//            }

//            if (unreadMessages.isNotEmpty()) {
//                unreadMessages.forEach { message ->
//                    FirebaseFirestore.getInstance()
//                        .collection(CHAT_COLLECTION)
//                        .document(chatId)
//                        .collection(MESSAGE_COLLECTION)
//                        .document(message.msgId).update("read", true)
//                    Log.d("ChatUI", "Updating unread messages ${message.msgId}")
//                }
//            }
//        else
//            {
//                Log.d("ChatUI", "Empty")
//            }
//        }
    }


    // For Updating the last read message which is visible on the chat ui
    LaunchedEffect(tp.last) {

        if (tp.last?.senderId != state.userData?.userId) {
            FirebaseFirestore.getInstance().collection(CHAT_COLLECTION).document(chatId)
                .update("last.read", true)
        }
        else if(state.userData?.userId == state.User2?.userId)
        {
            FirebaseFirestore.getInstance().collection(CHAT_COLLECTION).document(chatId)
                .update("last.read", true)
        }
    }

    val showNewMessagesButton = remember { mutableStateOf(false) }

    val showDownArrowButton = remember { mutableStateOf(false) }

    var currentIndex by remember { mutableIntStateOf(0) }

    var showDateChipCurrentIndex by remember { mutableIntStateOf(0) }

    var newMsgCount by remember { mutableIntStateOf(0) }

    var typingUsers by remember { mutableStateOf(emptyList<String>()) }

    // For the deleting of selected messages confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    // For the downloading of selected messages confirmation
    var showDownloadDialog by remember { mutableStateOf(false) }

    var selectedMsg : Message = Message()

    val context = LocalContext.current

    // For opening the images and video selector
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    { result ->
        val uri = result.data?.data

        uri?.let {
            val type = context.contentResolver.getType(it)

            val fileSize = context.contentResolver.openFileDescriptor(it, "r")?.use { pfd ->
                pfd.statSize
            } ?: -1

            val maxSize = 25 * 1024 * 1024L  // Max file size is 25MB
            if (fileSize > maxSize) {
                Toast.makeText(context, "File is too large! Max 25 MB allowed.", Toast.LENGTH_SHORT).show()
                return@let
            }

            viewModel.sendMediaMessage(uri, state.chatId, context)

        }
    }


    // Scroll related when new message come
    LaunchedEffect(messages.firstOrNull()) {

        newMsgCount = newMsgCount + 1

        val layoutInfo = lazyListState.layoutInfo
        val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()

        if(messages.isNotEmpty() && hasScrolledToUnread.value) {
            if (lazyListState.firstVisibleItemIndex == 1)
            {
                if (messages.firstOrNull()?.let { it.vidUrl.isNotEmpty() || it.imageUrl.isNotEmpty() } == true) // If image or video scroll to bottom
                {
                    lazyListState.scrollToItem(index = 0)
                }
                else if ((firstVisibleItem?.offset?: 0) >= -250 ) // If only about 250 pixels or less of msg is not visible only then scroll down
                {
                       lazyListState.scrollToItem(index = 0)

                       // below code if message too big show top of it all the times

                       if(messages.firstOrNull()?.let{it.senderId != state.userData?.userId} == true ) // only apply this for other user messages
                       {
                           val layoutInfo = lazyListState.layoutInfo
                           val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }
                           val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                           val itemHeight = targetItem?.size ?: 0

                           var offset = 0

                           if(messages.size == 1) offset = itemHeight
                           else if(itemHeight > (viewportHeight/2)) offset=itemHeight-(viewportHeight/2) - 100
                           else if(itemHeight > (viewportHeight/3)) offset=-(viewportHeight/3) - 100
                           else offset=-(viewportHeight/2) - 50

                           lazyListState.scrollToItem(0, offset)
                       }
                }
                else showNewMessagesButton.value = true
            }
            else if (messages[0].senderId == state.userData?.userId) {
                lazyListState.scrollToItem(index = 0)
            }
            else if(lazyListState.firstVisibleItemIndex  >= 1) {
                showNewMessagesButton.value = true
            }
        }
    }

    // To hide the reactions on scrolling
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect {
                if(showReactions)
                {
                    showReactions = false
                    showReactionsId = ""
                }
            }
    }

    // This is triggered once when lazy column is ready to scroll and then just listens continuously
    LaunchedEffect(lazyListState)
    {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { currentScroll ->

                if(currentScroll == 0)
                {
                    showNewMessagesButton.value = false
                    showDownArrowButton.value = false
                }
                else if(currentScroll == 1)
                {
                    delay(200) // So that its not showing for a split second when we get the new message
                    showDownArrowButton.value = true
                }
                else showDownArrowButton.value = true
                currentIndex = currentScroll
            }
    }

    val visibleDate = remember { mutableStateOf<String?>(null) }
    val showChip = remember { mutableStateOf(false) }


    // This is related with date overlay when scrolling
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index // Triggers when top visible item index in the lazy column changes
        }.distinctUntilChanged().collectLatest { index ->
            showDateChipCurrentIndex = currentIndex - newMsgCount // we did minus as on new msg adding it causes the chip to show up
        }
    }

    // This is to show date overlay when scrolling
    LaunchedEffect(showDateChipCurrentIndex) {

        val date = showDateChipCurrentIndex.let { messages.getOrNull(it)?.time?.toDate()?.toLocalDateOnly() }
        val formatted = formatDate(date)

        visibleDate.value = formatted
        showChip.value = true

        delay(1600)
        showChip.value = false
    }


    val lottieAnimationFile by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.chattypingindicator)
    )

//    var messageText by remember { mutableStateOf("") }
    val messageText = viewModel.reply

    // For the typing indicator showing
    val typingJob = remember { mutableStateOf<Job?>(null) }

    // Typing indicator related code
    LaunchedEffect(messageText) {
        val currentUserId = state.userData?.userId ?: return@LaunchedEffect

        if (messageText.isNotBlank()) {
            // Immediately set typing true
            TypingManager.setTyping(chatId, currentUserId, true)

            // Restart debounce job
            typingJob.value?.cancel()
            typingJob.value = CoroutineScope(Dispatchers.IO).launch {
                delay(1200) // Wait for 1s of no input
                TypingManager.setTyping(chatId, currentUserId, false)
            }
        } else {
            TypingManager.setTyping(chatId, currentUserId, false)
            typingJob.value?.cancel()
            typingJob.value = null
        }
    }

    // Initiate the observe typing status
    LaunchedEffect(state.chatId) {

        setCurrentChat(state.userData?.userId.toString(), state.chatId)

        val currentUserId = state.userData?.userId ?: return@LaunchedEffect
        observeTypingStatus(state.chatId, currentUserId) { updated ->
            typingUsers = updated
        }
    }


    Scaffold(
        topBar = {

            if(selectedMessages.isNotEmpty())
            {
                TopAppBar(
                    title = {
                        Text(text = selectedMessages.size.toString())
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectedMessages = emptyMap()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            copySelectedMessagesToClipboard(selectedMessages, context)
                            selectedMessages = emptyMap()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
            else
            {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
//                        .border(1.dp, Color.Red)
                                .padding(8.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            // Dismiss focus and hide keyboard when tapping outside the text field
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }
                                    )
                                }
//                        .border(1.dp, Color.White)
                            ,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = viewModel.userMetadataCache[userData.userId]?.third,
//                        model = userData.ppurl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(36.dp)
                            )
                            Column(
                                verticalArrangement = Arrangement.Center,
//                        modifier = Modifier.border(1.dp, Color.Yellow)
                            )
                            {
                                Text(
                                    text = viewModel.userMetadataCache[userData.userId]?.first.toString(),
//                            text = userData.username.toString(),
                                    modifier = Modifier.padding(start = 16.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
//                            color = MaterialTheme.colorScheme.onSurface
                                )

                                LastSeenText(presence?.second, presence?.first)

                                AnimatedVisibility(presence?.first == true) {
                                    // if its typing we show it otherwise we don't show
                                    Text(
                                        text = "Online",
                                        modifier = Modifier.padding(start = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp)
                                    )
                                }

                            }

                        }
                    },
                    navigationIcon = {
//                    Icon(
//                        Icons.Filled.ArrowBackIosNew,
//                        contentDescription = null
//                    )
                        IconButton(onClick = {
                            // This acts like the system back button
//                        activity?.onBackPressedDispatcher?.onBackPressed()
                            onBack()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton =
            {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .statusBarsPadding()
                        .imePadding()
                        .padding(bottom = 32.dp, top = 80.dp)
                )
                {
                    AnimatedVisibility(showChip.value && visibleDate.value != null && visibleDate.value != "",
                        modifier = Modifier.align(Alignment.TopCenter),
                        enter = fadeIn() + scaleIn(
                            transformOrigin = TransformOrigin.Center
                        ),
                        exit = fadeOut() + scaleOut(
                            transformOrigin = TransformOrigin.Center
                        )
                        )
                    {
                        Row(
                            modifier = Modifier.padding(bottom = if( (showNewMessagesButton.value || showDownArrowButton.value) &&
                                (tp.last?.msgId == messages.firstOrNull()?.msgId) )
                                0.dp else 48.dp )
//                                .align(Alignment.TopCenter)
                            ,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .offset(x = 0.dp, y = (0).dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                horizontalArrangement = Arrangement.Center,
//                            verticalAlignment = Alignment.CenterVertically

                            ) {
                                Text(
                                    text = visibleDate.value.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }


                    if(
                        (showNewMessagesButton.value || showDownArrowButton.value) && tp.last?.msgId == messages.firstOrNull()?.msgId
//                        visible = (showNewMessagesButton.value || showDownArrowButton.value) && tp.last?.msgId == messages.firstOrNull()?.msgId,
//                        modifier = Modifier.padding(bottom = 48.dp),
//                        enter = fadeIn(),
//                        exit = fadeOut()
                    )
                    {

                        Button(
                            onClick = {
                                if(messages.isNotEmpty())
                                {
                                    if(showNewMessagesButton.value)
                                    {
                                        coroutineScope.launch {
//                                    if (currentIndex > 10) {
//                                        lazyListState.scrollToItem(index = 10) // Jump closer first
//                                    }
                                            lazyListState.scrollToItem(index = 0)
                                            showNewMessagesButton.value = false

                                            // below code if message too big show top of it all the times

                                            val layoutInfo = lazyListState.layoutInfo
                                            val targetItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }
                                            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

                                            var itemHeight = targetItem?.size ?: 0

                                            var offset = 0

                                            if(messages.size == 1) offset = itemHeight
                                            else if(itemHeight > (viewportHeight/2)) offset=itemHeight-(viewportHeight/2) - 100
                                            else if(itemHeight > (viewportHeight/3)) offset=-(viewportHeight/3) - 100
                                            else offset=-(viewportHeight/2) - 50

                                            lazyListState.scrollToItem(0, offset)
                                        }
                                    }
                                    else
                                    {
                                        coroutineScope.launch {
//                                            if (currentIndex > 10) {
//                                                lazyListState.scrollToItem(index = 10) // Jump closer first
//                                            }
                                            lazyListState.scrollToItem(index = 0)
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp), // Custom corner radius
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                        {
                            Row(
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    top = 0.dp,
                                    end = 8.dp,
                                    bottom = 0.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Scroll to newest messages"
                                )
                                AnimatedVisibility(state.User2?.userId in typingUsers)
                                {
                                    Spacer(Modifier.width(8.dp))
                                    Text("Typing...", modifier = Modifier.padding(end = 2.dp))
                                }
                                AnimatedVisibility(showNewMessagesButton.value && state.User2?.userId !in typingUsers)
                                {
                                    Spacer(Modifier.width(8.dp))
                                    Text("New Message", modifier = Modifier.padding(end = 2.dp))
                                }
                            }
                        }
                    }
                }
            },
        floatingActionButtonPosition = FabPosition.Center

    )
    { it->

        // Dialog box for the confirmation for deleting the selected messages
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onDismiss = { showDeleteDialog = false
                    selectedMessages = emptyMap()
                            },
                onConfirm = {
                    deleteSelectedMessages(
                        selectedMessages,
                        viewModel,
                        state.chatId,
                        state
                    )
                    selectedMessages = emptyMap()
                    showDeleteDialog = false
                    showReactions = false
                    showReactionsId = ""
                }
            )
        }

        if(showDownloadDialog) {
            DownloadDialog (
                onDismiss = { showDownloadDialog = false
                },
                onConfirm = {
                    downloadFileFromMessage(context, selectedMsg)
                    selectedMessages = emptyMap()
                    showDownloadDialog = false
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize())
        {
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.1f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding()
                    .imePadding()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                // Dismiss focus and hide keyboard when tapping outside the text field
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        )
                    },
//            verticalArrangement = Arrangement.Bottom

            )
            {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp, top = 64.dp)
//                            .fillMaxSize()
//                    .clickable(interactionSource = remember { MutableInteractionSource() },indication = null) {
//                    focusManager.clearFocus()
//                }
                    ,
                    reverseLayout = true, // as we want to show the most recent messages at the last
                    state = lazyListState
                )
                {
                    itemsIndexed(messages, key = { _, msg -> msg.msgId }) { index, message ->

                        val prevMessage = messages.getOrNull(index - 1)
                        val nextMessage = messages.getOrNull(index + 1)

                        MessageItem(
                            message = message,
                            index = index,
                            prevId = prevMessage?.senderId.toString(),
                            nextId = nextMessage?.senderId.toString(),
                            state = state,
                            viewModel = viewModel,
                            navController = navController,
                            selectedMessages = selectedMessages,
                            onClick = { // For clicking in the video or video or file
                                if (selectedMessages.isNotEmpty()) {
                                    selectedMessages =
                                        if (selectedMessages.containsKey(message.msgId)) {
                                            selectedMessages - message.msgId
                                        } else {
                                            selectedMessages + (message.msgId to message)
                                        }
                                    if(selectedMessages.size > 1 || showReactions) {
                                        showReactions = false
                                        showReactionsId = ""
                                    }
                                }
                                else
                                {
                                    if(message.imageUrl.isNotEmpty())
                                    {
                                        viewModel.currentImageMessage = message
                                        navController.navigate(ImageScreen)
                                    }
                                    else if(message.vidUrl.isNotEmpty())
                                    {
                                        viewModel.currentImageMessage = message
                                        navController.navigate(VideoScreen)
                                    }
                                    else if(message.fileUrl.isNotEmpty())
                                    {
                                        if(message.fileUrl!="temp")
                                        {
                                            showDownloadDialog = true
                                            selectedMsg = message
                                        }
                                    }
                                }
                            },
                            onClick2 = { // For clicking in the area outside the message
                                if (selectedMessages.isNotEmpty()) {
                                    selectedMessages =
                                        if (selectedMessages.containsKey(message.msgId)) {
                                            selectedMessages - message.msgId
                                        } else {
                                            selectedMessages + (message.msgId to message)
                                        }
                                    if(selectedMessages.size > 1 || showReactions) {
                                        showReactions = false
                                        showReactionsId = ""
                                    }
                                }
                            },
//                            onLongClick = {
//                                selectedMessages = selectedMessages + (message.msgId to message)
//                            }
                            onLongPressAt = { offset, msg ->

                                if(selectedMessages.containsKey(msg.msgId)) // removes if already has the message selected
                                {
                                    selectedMessages = selectedMessages.filter { it.key != msg.msgId }
                                }
                                else
                                {
                                    selectedMessages = selectedMessages + (msg.msgId to msg)
                                    reactionOffset = offset
                                    if(selectedMessages.size > 1 || showReactions)
                                    {
                                        showReactions = false
                                        showReactionsId = ""
                                    } else {
                                        showReactions = true
                                        showReactionsId = msg.msgId
                                    }
                                    selectedReactionMessage = index
                                    if(selectedMessages.size == 1) haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress)
                                }
                            },
                            isSelected = selectedMessages.containsKey(message.msgId),
                        )

                        // Unread indicator in chat
                        if (unreadMarkerMessageId.value == message.msgId) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            )
                            {
                                Divider(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = " Unread ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Divider(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        val firstMsgDate = messages.getOrNull(0)?.time?.toDate()?.toLocalDateOnly()
                        val lastMsgDate =
                            messages.getOrNull(messages.size - 1)?.time?.toDate()?.toLocalDateOnly()

                        val prevDate2 =
                            messages.getOrNull(index + 1)?.time?.toDate()?.toLocalDateOnly()

                        val currentDate = message.time?.toDate()?.toLocalDateOnly()
                        val prevDate =
                            messages.getOrNull(index - 1)?.time?.toDate()?.toLocalDateOnly()

                        val showDateHeader2 = currentDate != prevDate2

                        val showDateHeader = currentDate != prevDate

                        // To insert a date header between messages from different days
                        if (((showDateHeader && prevDate != null && !(((index + 1) < messages.size) && (lastMsgDate == currentDate))) ||
                                    (((index + 1) == messages.size) && messages.size > 0) ||
                                    ((showDateHeader && prevDate == null && index == 0) && (messages.size > 1) && (showDateHeader2)) ||
                                    (index > 0 && (firstMsgDate == currentDate) && showDateHeader2))
                            && !(viewModel.isLoadingMore && messages.size > 15) // So that not show when loading indicator is on
                        ) {
//                                item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = formatDate(currentDate),
//                                        style = MaterialTheme.typography.labelMedium,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
//                                            .background(
//                                                MaterialTheme.colorScheme.surfaceVariant,
//                                                shape = RoundedCornerShape(16.dp)
//                                            )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
//                                }
                        } else if (nextMessage?.senderId != message.senderId) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                    }


                    // Loading indicator for pagination
                    item {
                        if (viewModel.isLoadingMore && messages.size > 15) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
//                        Log.d("ChatUI", "Message: ${messages.size}")


                    // Showing the start of the chat
                    if (viewModel.chatMessageCompleteLoaded[state.chatId] == true) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(
                                            vertical = 16.dp,
                                            horizontal = 32.dp
                                        ),
                                        text = "Messages start here...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }

                // typing animation
                AnimatedVisibility(state.User2?.userId in typingUsers && currentIndex == 0)
                {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 4.dp)
                            .width(84.dp)
                            .height(48.dp)
                            .background(
                                color = colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(
                                    topStart = 32.dp,
                                    topEnd = 32.dp,
                                    bottomEnd = 32.dp,
                                    bottomStart = 32.dp
                                )
                            )
//                        .border(1.dp, Color.White)
                        ,
                        contentAlignment = Alignment.Center
                    )
                    {
                        LottieAnimation(
                            composition = lottieAnimationFile,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier
//                                .background(
//                                Color.Black.copy(alpha = .2f),
//                                shape = RoundedCornerShape(24.dp))
                                .size(60.dp)
//                                .padding(24.dp)
//                                .border(1.dp, Color.Red)
                        )
                    }


//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Row(modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
//                            Text(
//                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
//                                text = "${state.User2?.username} is typing...",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            )
//                        }
//                    }
                }

//                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
                )
                {

                    IconButton(
                        onClick = {
                            focusManager.clearFocus()

                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                                addCategory(Intent.CATEGORY_OPENABLE) // only show openable files
                            }
                            val chooser = Intent.createChooser(intent, "Select a file")
                            launcher.launch(chooser)
                        },

                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.paperclip),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
//                        .focusRequester(focusRequester) // focus requester does not do anything in the row as its not focusable
//                        .background(
//                        color = MaterialTheme.colorScheme.surfaceContainer,
//                        shape = RoundedCornerShape(32.dp)
//                    )
                    )
                    {

                        // made a basic text field instead of normal text field as wanted to change the internal padding
                        BasicTextField(
                            value = viewModel.reply,
                            onValueChange = {
                                viewModel.reply = it
                            },
                            maxLines = 5,
                            textStyle = TextStyle(
                                color = colorScheme.onSurface,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            cursorBrush = SolidColor(colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    showReactions = false
                                    showReactionsId = ""
                                    selectedMessages = emptyMap()
                                    selectedReactionMessage = 0
                                }
                                .verticalScroll(rememberScrollState()), // Makes content scrollable vertically,

                            decorationBox = { innerTextField ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            shape = MaterialTheme.shapes.extraLarge
                                        )
                                        .padding(horizontal = 12.dp)
                                    ,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 12.dp)
                                    ) {
                                        if (viewModel.reply.isEmpty()) {
                                            Text(
                                                text = "Type a message",
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        innerTextField()
                                    }

                                    IconButton(
                                        onClick = {
                                            focusManager.clearFocus()
                                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                                type = "*/*"
                                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                                            }
                                            val chooser = Intent.createChooser(intent, "Select Media")
                                            launcher.launch(chooser)
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CameraAlt,
//                                            painter = painterResource(id = R.drawable.paperclip),
                                            contentDescription = "Attach",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        )

                        IconButton(
                            onClick = {
                                if (viewModel.reply.isNotBlank()) {
                                    TypingManager.setTyping(
                                        chatId,
                                        state.userData?.userId.toString(),
                                        false
                                    )
                                    typingJob.value?.cancel()
                                    typingUsers =
                                        typingUsers.filter { it != state.userData?.userId.toString() }
                                    viewModel.sendReply(
                                        msg = viewModel.reply,
                                        chatId = chatId,
                                        //   replyMessage = viewModel.replyMessage
                                    )
                                    viewModel.reply = ""
                                }
                            },

                            modifier = Modifier
                                .padding(start = 8.dp)
//                                .padding(horizontal = .dp, vertical = 12.dp) // Add padding around the button
                                .clip(CircleShape) // Clip to circle shape
                                .background(colorScheme.surfaceContainerHigh) // Set background color
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = null
                            )
                        }

//                    }

                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if(showReactions) {

//                ReactionTab(
////                    offset = reactionOffset,
////                    modifier = Modifier.offset(x=-(-200).dp,y= 100.dp),
//                    onDismiss = {
//                        showReactions = false
////                            selectedMessages = emptyMap()
//                    },
//                    onReactionSelected = { reaction ->
//                        showReactions = false
//                        // handle reaction
//                    }
//                )
                   val visibleInfo = lazyListState.layoutInfo.visibleItemsInfo // no need for remember here as if list changes the reaction disappear as well
                   val selectedItem = visibleInfo.find { it.index == selectedReactionMessage }

                   val popupOffset = if (selectedItem != null) {
                       // Adjust X and Y if needed
                       IntOffset(
                           x = reactionOffset.x.toInt(),
                           y = (lazyListState.layoutInfo.viewportEndOffset - selectedItem.offset - (selectedItem.size - reactionOffset.y.toInt()))
                       )
                   } else {
                       IntOffset.Zero
                   }

                   Popup(
                       offset = popupOffset,
                       properties = PopupProperties(
                           focusable = false,
                           dismissOnBackPress = true,
                       )
                   ) {
                       Box(modifier = Modifier.padding(8.dp))
                       {
                           ReactionTab(
                               onDismiss = {
                                   showReactions = false
                                   selectedMessages = emptyMap()
                                   showReactionsId = ""
                               },
                               onReactionSelected = { emoji ->
                                   showReactions = false
                                   showReactionsId = ""
                               },
                               message = messages[selectedReactionMessage],
                               viewModel = viewModel,
                               userId = state.userData?.userId.toString(),
                               onClick = {
                                   selectedMessages = emptyMap()
                                   showReactions = false
                                   showReactionsId = ""
                               }
                           )
                       }
                   }
            }

        }
    }
}

@Composable
fun MessageItem(message: Message, index: Int, prevId: String, nextId: String, state: AppState, viewModel: ChatViewModel,
                navController: NavController, selectedMessages: Map<String, Message>,
                onClick: () -> Unit,
                onClick2: () -> Unit,
//                onLongClick: () -> Unit
                onLongPressAt: ((Offset, Message) -> Unit)? = null,
                isSelected: Boolean,
) {
//    Log.d("Control", "Working")
    val context = LocalContext.current
//    val brush = Brush.linearGradient(
//        listOf(
//            MaterialTheme.colorScheme.primaryContainer,
//            MaterialTheme.colorScheme.tertiaryContainer
//        ),
////        start = Offset(0f, 0f),
////        end = Offset(1000f, 1000f)
//    )
//    val brush2 = Brush.linearGradient(
//        listOf(
//            MaterialTheme.colorScheme.secondary,
//            MaterialTheme.colorScheme.primary
//
//        )
//    )
    val isCurrentUser = state.userData?.userId == message.senderId

    val overlayColor = if (!isSelected) Color.Transparent else colorScheme.onPrimary.copy(alpha = 0.4f)

    val sizes = 24.dp
    val sizes2 = 27.dp
    val smsizes = 4.dp

    // Animate corner radii individually
    val topStartTarget = when {
        isCurrentUser -> sizes2
        prevId == message.senderId && nextId == message.senderId -> smsizes
        prevId == message.senderId -> sizes
        nextId == message.senderId -> smsizes
        else -> sizes2
    }

    val topEndTarget = when {
        isCurrentUser && prevId == message.senderId && nextId == message.senderId -> smsizes
        isCurrentUser && prevId == message.senderId -> sizes
        isCurrentUser && nextId == message.senderId -> smsizes
        isCurrentUser -> sizes2
        else -> sizes2
    }

    val bottomEndTarget = when {
        isCurrentUser && prevId == message.senderId && nextId == message.senderId -> smsizes
        isCurrentUser && prevId == message.senderId -> smsizes
        isCurrentUser && nextId == message.senderId -> sizes
        isCurrentUser -> sizes2
        else -> sizes2
    }

    val bottomStartTarget = when {
        isCurrentUser -> sizes2
        prevId == message.senderId && nextId == message.senderId -> smsizes
        prevId == message.senderId -> smsizes
        nextId == message.senderId -> sizes
        else -> sizes2
    }

    val animationSpec = tween<Dp>(
        durationMillis = 800,
        easing = FastOutSlowInEasing
    )

//  Animate corners smoothly
    val topStart by animateDpAsState(topStartTarget, animationSpec, label = "topStart")
    val topEnd by animateDpAsState(topEndTarget, animationSpec, label = "topEnd")
    val bottomEnd by animateDpAsState(bottomEndTarget, animationSpec, label = "bottomEnd")
    val bottomStart by animateDpAsState(bottomStartTarget, animationSpec, label = "bottomStart")

//  Final animated shape
    val shape = RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomEnd = bottomEnd,
        bottomStart = bottomStart
    )



//    val color = if (isCurrentUser) brush else brush2 // which color

    val color = if (isCurrentUser) colorScheme.primaryContainer else (colorScheme.surfaceContainerHigh) // which color

    val alignment = if(isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart // which side

//    val formatter = remember {
//        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
//    }
//
//    val interactionSource = remember { MutableInteractionSource() }
//    val indication: Indication = LocalIndication.current // Replaces deprecated rememberRipple()

    val lastReadTimestamp = viewModel.lastReadTimestamp.value

    val isSelected = selectedMessages.containsKey(message.msgId)

    Box(modifier = Modifier
//        .indication(interactionSource, indication)
        .background(
            overlayColor
        )
        .fillMaxWidth()
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { offset ->
                    onLongPressAt?.invoke(offset, message)
                },
                onTap = {
                    onClick2()
                }
            )
        }
        ,
        contentAlignment = alignment
    ) {
        Column (
            verticalArrangement = Arrangement.Bottom,

            modifier = Modifier.padding(top=2.dp, end=if(isCurrentUser) 8.dp
            else 0.dp, start=if(isCurrentUser) 0.dp else 8.dp,
                bottom = if(message.reactReceiver!="" || message.reactSender!="") if(viewModel.tp.last?.msgId == message.msgId) 24.dp else 8.dp else 0.dp
                // If reactions then padding at bottom also if its the 0th index of lazy column then more padding
                )
//                .border(1.dp, Color.Magenta)
        )
        {
            Column(
                modifier = Modifier
//                    .shadow(2.dp, shape = shape)
                    .widthIn(max = 270.dp)
                    .fillMaxHeight()
                    .background(if (isSelected) colorScheme.primary else color, shape)
                    .align(if (isCurrentUser) Alignment.End else Alignment.Start)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                onLongPressAt?.invoke(offset, message)
                            },
                            onTap = {
                                onClick()
                            }
                        )
                    }
                ,
                horizontalAlignment = Alignment.End
            ) {
                if(message.content != "" || message.imageUrl.isNotEmpty() || message.vidUrl.isNotEmpty())
                {
                    if (message.imageUrl.isNotEmpty())
                    {
                        SubcomposeAsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Image message",
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .fillMaxWidth(0.8f)
                                .defaultMinSize(minHeight = 100.dp)
                                .heightIn(max = 300.dp),
//                            .aspectRatio(1f),    // this gives square image
                            contentScale = ContentScale.Crop,
                            loading = {
                                LoadingIndicator()
                            },
                            error = {
                                LoadingIndicator()
                            }
                        )
                    }
                    else if(message.fileUrl.isNotEmpty())
                    {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(start = 6.dp, end = 4.dp, bottom = 8.dp)
                        )
                        {
                            if(message.fileUrl!="temp")
                            {
                                Image(
                                    painter = painterResource(id = R.drawable.doc_img2),
                                    modifier = Modifier
                                        .padding(top = 12.dp, bottom = 2.dp, start = 2.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .size(96.dp)
                                        .background(colorScheme.secondary)
                                    ,
                                    contentDescription = "Image message",
                                )
                            }
                            else
                            {
                                LoadingIndicator()
                            }

                            Text(text = message.content.toString(),
//                        modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp),
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 4.dp, end = 8.dp)
                                    .fillMaxWidth(0.8f)
//                            .padding(start = if (isCurrentUser) 8.dp else 16.dp, end = if (isCurrentUser) 16.dp else 8.dp, top = 4.dp, bottom = 4.dp)
//                            .padding(start = if (isCurrentUser) 16.dp else 16.dp, end = if (isCurrentUser) 16.dp else 16.dp, top = 8.dp, bottom = 6.dp)

//                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp)
//                            .padding(vertical = 4.dp)
                                    .align(if (isCurrentUser) Alignment.End else Alignment.Start)
//                            .border(1.dp, Color.Red)
                                ,
                                color = if(isSelected) colorScheme.onPrimary else colorScheme.onBackground,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                                )
                        }
                    }
                    else if(message.vidUrl.isNotEmpty())
                    {
                        Box(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
//                                .clickable {
//                                    viewModel.currentImageMessage = message
//                                    navController.navigate(VideoScreen)
//                                }
//                                .combinedClickable (
//                                    onClick = onClick,
//                                    onLongClick = onLongClick
//                                )
                                ,
                            contentAlignment = Alignment.Center
                        )
                        {
                            val model = ImageRequest.Builder(context)
                                .data(message.vidUrl) // Use the video URL directly
                                .videoFrameMillis(1000)
                                .decoderFactory { result, options, _ ->
                                    VideoFrameDecoder(result.source, options)
                                }
//                                .crossfade(true)
//                                .videoFrameMillis(10000)
//                                .decoderFactory { result, options, _ ->
//                                    VideoFrameDecoder(result.source, options)
//                                }
                                .build()

                            SubcomposeAsyncImage(
                                model = model,
                                contentDescription = "Video message",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .defaultMinSize(minHeight = 100.dp)
                                    .heightIn(max = 300.dp),
                                loading = {
                                    LoadingIndicator()
                                },
                                error = {
                                    LoadingIndicator()
                                }
                            )

                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    if(message.content != "" && message.fileUrl.isEmpty())
                    {
                        Text(text = message.content.toString(),
//                        modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp),
                            modifier = Modifier
//                            .padding(start = if (isCurrentUser) 8.dp else 16.dp, end = if (isCurrentUser) 16.dp else 8.dp, top = 4.dp, bottom = 4.dp)
//                            .padding(start = if (isCurrentUser) 16.dp else 16.dp, end = if (isCurrentUser) 16.dp else 16.dp, top = 8.dp, bottom = 6.dp)

                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp)
//                            .padding(vertical = 4.dp)
                                .align(if (isCurrentUser) Alignment.End else Alignment.Start)
//                            .border(1.dp, Color.Red)
                            ,
                            color = if(isSelected) colorScheme.onPrimary else colorScheme.onBackground,

                            )
                    }

                    Row(
                        modifier = Modifier
//                    .border(1.dp, Color.White)
//                            .align(if (isCurrentUser) Alignment.End else Alignment.End)
                            .align(Alignment.End)
//                            .offset(x=0.dp, y=(-4).dp)
//                            .border(1.dp, Color.White)
//                            .padding(start = if(isCurrentUser) 16.dp else 28.dp, end = if(isCurrentUser) 16.dp else 16.dp)

                            .padding(start = if (isCurrentUser) 16.dp else 28.dp, end = 16.dp,
                                bottom = if(message.reactReceiver!="" || message.reactSender!="") 4.dp else 0.dp
                                )
                            .offset(x = 0.dp, y = (-4).dp)
//                            .border(1.dp, Color.Blue)
                        ,
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment  = Alignment.CenterVertically
                    )
                    {
//                    val formatter = SimpleDateFormat("h:mm a · ", Locale.getDefault())
                        val formatter = SimpleDateFormat("h:mm ", Locale.getDefault())
                        Text(
                            text = message.time?.toDate()?.let { formatter.format(it) } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if(isSelected) colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if(isCurrentUser)
                        {
                            Icon(
//                                imageVector = if (message.read) Icons.Filled.DoneAll else Icons.Filled.DoneAll,
                                painter = if ((message.time!= null && lastReadTimestamp >= message.time) ||

                                    // meaning you are texting yourself
                                    (state.User2?.userId == state.userData.userId)) painterResource(id = R.drawable.doublecheckcolored)
                                else painterResource(id = R.drawable.doublecheck),
//                            painter = painterResource(id = R.drawable.check_mark),
                                contentDescription = null,
                                modifier = Modifier
//                            .align(Alignment.CenterEnd)
//                                    .padding(end = 0.dp)
//                                    .size(12.dp)
                                    .size(20.dp)
//                                    .offset(x=0.dp, y=(-4).dp)
//                            .offset(x=(-8).dp, y=(-8).dp)
                                ,
                                tint = if(isSelected) colorScheme.onPrimary else colorScheme.onBackground
//                            tint = if (chat.last?.read ?: false) Color.Green else Color.Gray
//                        tint = if (message.read) Color(79,182,236) else Color(170,170,170)  // WhatsApp blue-green

//                                tint = if (message.read) Color(79,182,236) else MaterialTheme.colorScheme.onSurfaceVariant  // WhatsApp blue-green

//                                tint = if (message.time!= null && lastReadTimestamp >= message.time) Color(79,182,236) else MaterialTheme.colorScheme.onSurfaceVariant  // WhatsApp blue-green

                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(if(isCurrentUser) Alignment.BottomEnd else Alignment.BottomStart)
                .offset(x = if(isCurrentUser) (-8).dp else 8.dp,
                    y=if(viewModel.tp.last?.msgId == message.msgId) (-6).dp
                    else 9.dp)
        )
        {
            if(message.reactReceiver!="" && message.reactSender!="")
            {
                if(message.reactSender == message.reactReceiver)
                {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(message.reactSender, fontSize = 16.sp)
                        }

                        Box(modifier = Modifier.offset(x=12.dp, y=(-9).dp),
                            contentAlignment = Alignment.TopCenter
                        )
                        {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainerLow,
                                        shape = CircleShape
                                    ),
                            ) {
                                Text(
                                    "2",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .offset(x = 5.dp, y = (-4).dp)
                                )
                            }
                        }
                    }

                }
                else
                {
                    Row()
                    {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(message.reactReceiver, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(message.reactSender, fontSize = 16.sp)
                        }
                    }
                }
            }
            else if(message.reactReceiver!="")
            {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(message.reactReceiver, fontSize = 16.sp)
                }
            }
            else if(message.reactSender!="")
            {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(message.reactSender, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator()
{
    Box(
        modifier = Modifier
            .padding(8.dp)              // Outer padding if needed
            .size(200.dp),              // Total box size
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
