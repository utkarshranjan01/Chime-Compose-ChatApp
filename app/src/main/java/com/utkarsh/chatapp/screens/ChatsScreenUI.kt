package com.utkarsh.chatapp.screens

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.utkarsh.chatapp.AppState
import com.utkarsh.chatapp.ChangeInfoPage
import com.utkarsh.chatapp.ChatData
import com.utkarsh.chatapp.ChatUserData
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.NavigationState
import com.utkarsh.chatapp.R
import com.utkarsh.chatapp.dialogs.CustomDialogBox
import com.utkarsh.chatapp.dialogs.DeleteConfirmationDialog
import com.utkarsh.chatapp.utilities.TimeDisplay.LastSeenText2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.min


@Composable
fun ChatScreenUI(
    viewModel: ChatViewModel,
    state: AppState,
    showSingleChat: (ChatUserData, String) -> Unit = { _, _ -> },
    navController: NavController
) {

//    val scrollState = rememberScrollState()
//    var isFabExpanded by remember { mutableStateOf(true) }
//    var lastScrollPosition by remember { mutableStateOf(0) }

    var isFabExpanded by remember { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableStateOf(0) }
    val lazyListState = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val padding by animateDpAsState(targetValue = 10.dp, label = "")
    val chats = viewModel.chats
    val filterChats = chats
    val selectedItem = remember { mutableStateListOf<String?>() }

    val isChatSelected = selectedItem.isNotEmpty()

    var expanded by remember { mutableStateOf(false) }


    // For the deleting of selected messages confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    val localContext  = LocalContext.current

//    val valueUser = state.userData?.userId.toString()
//    TrackPresence(valueUser)

//    val visibleChats by remember {
//        derivedStateOf {
//            val layoutInfo = lazyListState.layoutInfo
//            val start = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
//            val end = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
//            chats.subList(start.coerceAtLeast(0), (end + 1).coerceAtMost(chats.size))
//        }
//    }

    // This variable will store the current visible chats on the screen and as the scroll state changes it changes
    var visibleChats by remember { mutableStateOf<List<ChatData>>(emptyList()) }

    // Track scroll direction and update FAB state
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { currentScroll ->
                Log.d("CHATUI78","$currentScroll")
                val isScrollingDown = currentScroll > lastScrollPosition
                if (isScrollingDown) isFabExpanded = false else isFabExpanded = true
                lastScrollPosition = currentScroll

                val start = currentScroll
                val visibleCount = lazyListState.layoutInfo.visibleItemsInfo.size
                val end = (start + visibleCount).coerceAtMost(filterChats.size)
//                val visibleCount = 10
//                val end = (start + visibleCount).coerceAtMost(filterChats.size)

                if (filterChats.isNotEmpty() && visibleCount > 0) {
                    visibleChats = chats.subList(start, end)
//                    Log.d("ChatUI67", "WORKING")
                }
//                else
//                {
//                    Log.d("ChatUI67", "NOT WORKING ${filterChats.size} ${visibleCount}")
//                }
            }
    }


    // It is running initial load of the chats basically filter chats gets all the chats this gets first 10 of those chats
    // Its also just fetching the metadata of all the users its lightweight so we do for all the users
    LaunchedEffect(filterChats) {

        visibleChats = filterChats.subList(lastScrollPosition, (lastScrollPosition + 10).coerceAtMost(filterChats.size))

        filterChats.forEach { chat ->
            viewModel.fetchUserMetadata(chat.user1?.userId.toString())
            viewModel.fetchUserMetadata(chat.user2?.userId.toString())
        }

    }

    LaunchedEffect(visibleChats) {

//        Log.d("ChatUI67", "${visibleChats.size}")

        viewModel.messageVersion.intValue = 0

//        if (viewModel.chatMessagesPreloaded.size != viewModel.chats.size) {
//        coroutineScope.launch(Dispatchers.IO) {
        visibleChats.forEach { chat ->

            // Basically getting the userId of the other user
            val chatUser = if(chat.user1?.userId.toString() == state.userData?.userId.toString()) chat.user2?.userId.toString() else chat.user1?.userId.toString()

//            Log.d("ChatUI67", "ChatUser: $chatUser")
            viewModel.observeUserPresence(chatUser)

                val chatId = chat.chatId
                if (!viewModel.chatMessagesPreloaded.containsKey(chatId)) {
                    viewModel.loadInitialMessages2(chatId)
                }
            }

        launch(Dispatchers.Default) {

            // If unread less than 50 and so if more that 50 messages preloaded only keep first 50 messages

            var unreadCount = (if(state.userData?.userId == viewModel.tp.user1?.userId) viewModel.tp.user1?.unread else viewModel.tp.user2?.unread)?:0
            unreadCount += 5 // If its more than total messages it returns all the message so no issue
            val pageSize = if(50 >= unreadCount) 50 else unreadCount

            if (!viewModel.lastChatId.isNullOrBlank()) {
                val messagesList = viewModel.chatMessagesPreloaded[viewModel.lastChatId ?: ""]
                if (messagesList != null && messagesList.size > pageSize) {
                    messagesList.removeRange(pageSize, messagesList.size)
                    viewModel.chatMessageCompleteLoaded[viewModel.lastChatId ?: ""] = false
                }
                viewModel.lastChatId = null
            }

            // If more that 15 chats preloaded remove chats which are not viewable currently by the user
            if (viewModel.chatMessagesPreloaded.size > 15) {
//                Log.d("ChatScreenUI", "Size: ${viewModel.chatMessagesPreloaded.size}")
                val visibleChatIds = visibleChats.map { it.chatId }.toSet()

                // Get all cached chat IDs that are NOT in the current filter
                val toRemove = viewModel.chatMessagesPreloaded.keys - visibleChatIds

                // Remove all chats that are not in the current filter
                toRemove.forEach { chatId ->
                    viewModel.chatMessagesPreloaded.remove(chatId)
                    viewModel.chatMessageCompleteLoaded.remove(chatId)
                }
//                Log.d("ChatScreenUI", "Size: ${viewModel.chatMessagesPreloaded.size}")
            }
        }
//        }
//        }
    }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                onDismiss = {
                    showDeleteDialog = false
                    selectedItem.clear()
                },
                onConfirm = {
                    selectedItem.forEach { id ->
                        id?.let {
                            viewModel.deleteChat(it)
                        }
                    }
                    selectedItem.clear()
                    showDeleteDialog = false
                }
            )
        }



        Scaffold(
            floatingActionButton =
                {
//                AnimatedVisibility( // <- AnimatedVisibility is the outer composable
//                    visible = visibility,
//                    enter = scaleIn(
//                        tween(300)
////                        animationSpec = tween(durationMillis = 300),
////                        initialScale = 0.5f // Optional: Start at half size
//                    ),
//                    exit = scaleOut(
//                        tween(300)
////                        animationSpec = tween(durationMillis = 300),
////                        targetScale = 0.5f // Optional: Shrink to half size
//                    )
////                    enter = fadeIn(animationSpec = tween(durationMillis = 500)),
////                    exit = fadeOut(animationSpec = tween(durationMillis = 500))
//                ) {
                    FloatingActionButton(
                        onClick = { viewModel.showDialog() },
//                    shape = RoundedCornerShape(15.dp),
                        shape = shapes.large,
//                    containerColor = colorScheme.inversePrimary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = null,
//                        tint = colorScheme.onPrimary
                            )
                            AnimatedVisibility(
                                visible = isFabExpanded,
                                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Start chat")
                                }
                            }
                        }
                    }
//                }
            },
            bottomBar =
                {
                NavigationBar {
                }
                // added empty bottom bar here as it was not adding padding from main one if we passed padding it was creating a gap on top
                // the navigation bar here would not be displayed that's why kept it empty just for padding as it always defaults to the root one
//                    BottomNavigationBar(navController = navController)
            }
        )

        { innerPadding ->

            AnimatedVisibility(
                visible = state.showDialog,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight }, // Slide up from bottom
                    animationSpec = tween(300) // Smooth 300ms transition
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight }, // Slide down out of screen
                    animationSpec = tween(300)
                )
            )
            {
                CustomDialogBox(
                    state = state,
                    hideDialog = { viewModel.hideDialog()
                        viewModel.setSrEmail("")},
                    addChat = {
                        viewModel.addChat(state.srEmail, context = localContext)
                        viewModel.hideDialog()
                        viewModel.setSrEmail("")
                    },
                    setEmail = {
                        viewModel.setSrEmail(it) // this it is its own email
                    }

                )
            }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                // Dismiss focus and hide keyboard when tapping outside the text field
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            },
                        )
                    }
            )
            {
                if (isChatSelected)
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        IconButton(modifier = Modifier,
                            onClick = { selectedItem.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                        Text("${selectedItem.size} selected", modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            )
                        IconButton(onClick = {
                                showDeleteDialog = true
                            }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                            }
                    }
                }
                else
                {
                    Box(
                        modifier = Modifier
//                    .clickable(interactionSource = remember { MutableInteractionSource() },indication = null) {
//                    focusManager.clearFocus()
//                }
                    )
                    {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    //                            .fillMaxWidth(0.98f),
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp),
    //                        horizontalArrangement = Arrangement.SpaceBetween
                            )
                            {
                                Text(
                                    text = "Chime",
    //                                modifier = Modifier.padding(start = 16.dp).offset(y = 5.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                )

    //                        Spacer(modifier = Modifier.weight(1f))
    //                        IconButton(
    //                            onClick = {}, modifier = Modifier.background(
    //                                colorScheme.background.copy(alpha = 0.2f),
    //                                shape = CircleShape
    //                            ).border(
    //                                .05.dp,
    //                                color = Color(0xFF35567A),
    //                                shape = CircleShape,
    //
    //                                )
    //                        ) {
    //                            Icon(
    //                                painter = painterResource(id = R.drawable._666693_search_icon),
    //                                contentDescription = null,
    //                                modifier = Modifier.scale(0.7f)
    //                            )
    //                        }
                                Column {
                                    IconButton(
                                        onClick = {
                                            expanded = true
    //                                focusManager.clearFocus()
                                        },
                                        modifier = Modifier.offset(x = 8.dp,y = 0.dp)
    //                                modifier = Modifier.background(colorScheme.background.copy(alpha = 0.2f), CircleShape)
    //                                    .border(0.05.dp, Color(0xFF35567A), CircleShape)
                                    ) {
    //                                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null, modifier = Modifier.scale(1.3f))
                                        AsyncImage(
                                            // this loads image from the internet

                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(viewModel.userMetadataCache[state.userData?.userId]?.third)
    //                                        .data(state.userData?.ppurl)
                                                .crossfade(false)
                                                .allowHardware(true)
    //                .listener(
    //                onSuccess = { _, _ -> isImageLoaded = true }
    //            )
                                                .build(),
    //            placeholder = painterResource(R.drawable.person_placeholder_4),
//                                            error = painterResource(R.drawable.person_placeholder_4),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(35.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = {
                                            expanded = false
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceBright),
    //                                    modifier = Modifier.background(colorScheme.background)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Profile",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center,
                                                )
                                            },
                                            onClick = {
                                                NavigationState.currentRoute = "ChangeInfoPage"
                                                navController.navigate(ChangeInfoPage) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                        }
                    }
                }

                SearchBar(
                    query = viewModel.searchQuery,
                    onQueryChange = { viewModel.searchQuery = it },
                )

                /*
                Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))
                {
                    BasicTextField(
                        value = "",
                        onValueChange = {},
                        maxLines = 1,
                        textStyle = androidx.compose.ui.text.TextStyle(
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
//                            .verticalScroll(rememberScrollState()), // Makes content scrollable vertically
                                ,
                        decorationBox = { innerTextField ->
                            TextFieldDefaults.DecorationBox(
                                value = viewModel.reply,
                                innerTextField = innerTextField,
                                enabled = true,
                                singleLine = false, // or true, depending on your need
                                visualTransformation = VisualTransformation.None,
                                placeholder = @Composable {
                                    Text(
                                        text = "Search in chat",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                ),
//                                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp),
                                interactionSource = remember { MutableInteractionSource() },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search",
//                                tint = Color.Gray,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                },
                            )
                        }
                    )
                }
                */

                val filterChats2 = viewModel.chats.filter { chat ->
                    val otherUser = if (chat.user1?.userId == state.userData?.userId) chat.user2 else chat.user1
                    otherUser?.username?.contains(viewModel.searchQuery, ignoreCase = true) ?: false
                }

//                val context =  LocalContext.current
//                val count = remember { mutableStateOf(0) }
//                Button(onClick = {
//                    fetchAndUploadUserWithStory(viewModel, context)
//                    count.value++
//                }) {
//                    Text("Add Users")
//                }
//                Text("Added Users ${count.value}")

                LazyColumn(
                    state = lazyListState,
                )
                {
                    items(
                        items = filterChats2,
                        key = { chat -> chat.chatId }
                    )
                    { it ->
//                        val chatUser = if (
//                            it.user1?.userId == state.userData?.userId
//                        // this means you yourself are the user
//                        ) {
//                            it.user2
//                        } else {
//                            it.user1
//                        }

                        val chatUser =
                            if (it.user1?.userId == state.userData?.userId) it.user2 else it.user1
//                        val isVisible = visibleMap[it.chatId] == true
//
//                        // Launch animation trigger only once per chat
//                        LaunchedEffect(Unit) {
//                            visibleMap[it.chatId] = true
//                        }

//                        AnimatedVisibility(
//                            visible = isVisible.value,
//                            enter = fadeIn(),
//                            exit = fadeOut()
//                        ) {
                        if (chatUser != null) { // Display only if the other chat user exists
                            ChatItem(
                                state = state,
                                userData = chatUser,
                                chat = it,
                                isSelected = selectedItem.contains(it.chatId),
                                viewModel,
                                onClick = {
                                    if (selectedItem.isNotEmpty()) {
                                        if (selectedItem.contains(it.chatId)) selectedItem.remove(
                                            it.chatId
                                        )
                                        else selectedItem.add(it.chatId)
                                    } else {
                                        keyboardController?.hide()
                                        viewModel.isNavigating = true
                                        showSingleChat(chatUser, it.chatId)
                                    }
                                },
                                onLongClick = {
                                    if (!selectedItem.contains(it.chatId)) selectedItem.add(it.chatId)
                                }
                            )
                        }
//                        }
                    }
                }

                // This displays when no chats exist or you search something which has no result
                if(filterChats2.isEmpty() && viewModel.isChatsInitialized)
                {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 64.dp, bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        if(viewModel.chats.isEmpty()) Text("Add people to start chatting", style = MaterialTheme.typography.titleLarge)
                        else Text("No users found you can add more", style = MaterialTheme.typography.titleLarge)

                        Spacer(modifier = Modifier.height(64.dp))
                        Image(
                            painter = painterResource(id = R.drawable._073773),
                            contentScale = ContentScale.Crop,
                            contentDescription = "Empty Chats Screen",
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .size(300.dp)
                        )
                    }
                }
            }
        }

}


@Composable
fun ChatItem(
    state: AppState,
    userData: ChatUserData,
    chat: ChatData,
    isSelected: Boolean = false,
    viewModel: ChatViewModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {

    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }

    val color = if (!isSelected) Color.Transparent else colorScheme.onPrimary

    // Animation state
    var isLoading by remember { mutableStateOf(false) }

    val otherUser = if (chat.user1?.userId == state.userData?.userId) chat.user2?.userId else chat.user1?.userId
    val presence = viewModel.userPresenceMap[otherUser.toString()]
    val isOnline = presence?.first ?: false
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color,
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )


        ,
//            .border(
//                width = 1.dp,
//                color = Color.Green
//            )
//            .alpha(alpha)
//        verticalAlignment = Alignment.CenterVertically
    )
    {
        Row (
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .height(60.dp)
//                .border(1.dp, Color.Yellow)
            ,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            AsyncImage(
                // this loads image from the internet

                model = ImageRequest.Builder(LocalContext.current)
                    .data(viewModel.userMetadataCache[userData.userId]?.third)
//                    .data(userData.ppurl)
                    .crossfade(false)
                    .allowHardware(true)
//                .listener(
//                onSuccess = { _, _ -> isImageLoaded = true }
//            )
                    .build(),
//            placeholder = painterResource(R.drawable.person_placeholder_4),
//                error = painterResource(R.drawable.person_placeholder_4),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
//                    .border(1.dp, Color.Green)
                    .alpha(if (isLoading) 1f else 0f),
                onSuccess = { isLoading = true },
                onError = { isLoading = true }
            )

            Spacer(modifier = Modifier.width(16.dp))


//        AnimatedVisibility(
//            visible = isImageLoaded,
////            enter = fadeIn()
//        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isLoading) 1f else 0f)
                // above alpha line to make image and text load together
//                .border(
//                    width = 1.dp,
//                    color = Color.Red,
//                )
//                .padding(top = 4.dp)
                , verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth(.95f)
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
//                    .border(
//                    width = 1.dp,
//                        color = Color.White),
//                )
                ) {
                    Text(
                        text = if (userData.userId == state.userData?.userId)
                            viewModel.userMetadataCache[userData.userId]?.first.orEmpty() + " (You)" else viewModel.userMetadataCache[userData.userId]?.first.orEmpty(),
//                        text = if (userData.userId == state.userData?.userId)
//                            userData.username.orEmpty() + " (You)" else userData.username.orEmpty(),
//                    modifier = Modifier.width(width = 200.dp),
                        modifier = Modifier.widthIn(max = 200.dp),
                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = if(chat.last?.read == true || chat.last?.senderId == state.userData?.userId) FontWeight.Normal else FontWeight.Bold,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    AnimatedVisibility(
                        visible = isOnline && otherUser != state.userData?.userId.toString(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
//                            .border(1.dp,Color.White)
//                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            // Dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())


                    LastSeenText2(chat.last?.time?.toDate()?.time)
//                    Text(
//                        text = formatLastSeenStyle2(chat.last?.time?.toDate()?.time),
////                        text = chat.last?.time?.toDate()?.let { formatter.format(it) } ?: "",
//                        color = Color(170, 170, 170),
//                        style = MaterialTheme.typography.titleSmall.copy(
//                            fontWeight = FontWeight.Light
//                        ),
////                    modifier = Modifier.border(1.dp,Color.White)
//                    )
                }

//                AnimatedVisibility(chat.last?.time != null) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
//                        .height(40.dp)
                    ) {
                        // Send and read indicators
                        if (chat.last?.senderId == state.userData?.userId) {
                            Icon(
//                            imageVector = if (chat.last?.read == true) Icons.Filled.DoneAll else Icons.Filled.DoneAll,
                                painter = if (chat.last?.read == true) painterResource(id = R.drawable.doublecheckcolored) else painterResource(
                                    id = R.drawable.doublecheck
                                ),
//                            painter = painterResource(id = R.drawable.check_mark),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 5.dp)
//                                .size(16.dp)
                                    .size(20.dp),
//                            tint = if (chat.last?.read ?: false) Color.Green else Color.Gray
//                            tint = if (chat.last?.read == true) Color(79,182,236) else Color(170,170,170)  // WhatsApp blue-green

//                            tint = if (chat.last?.read == true) Color(79,182,236) else MaterialTheme.colorScheme.onSurfaceVariant  // WhatsApp blue-green
                            )
                        }
//                    chat.last?.text?.let { Text(it) }


                        // Message text
                        var msgText = if(chat.last?.imageUrl?.isNotEmpty()?:false) "Sent a photo"
                                        else if(chat.last?.vidUrl?.isNotEmpty()?:false) "Sent a video" else chat.last?.content?:""

                        val msgText1: String = msgText

                        if(chat.last?.reactSender!="" && chat.last?.senderId == state.userData?.userId)
                        {
                            msgText = "You ${chat.last?.reactSender} to $msgText1"
                        }
                        if(chat.last?.reactReceiver!="" && chat.last?.senderId != state.userData?.userId)
                        {
                            msgText = "You ${chat.last?.reactReceiver} to $msgText1"
                        }
                        if(chat.last?.reactSender!="" && chat.last?.senderId != state.userData?.userId)
                        {
                            msgText = "They ${chat.last?.reactSender} to $msgText1"
                        }
                        if(chat.last?.reactReceiver!="" && chat.last?.senderId == state.userData?.userId)
                        {
                            msgText = "They ${chat.last?.reactReceiver} to $msgText1"
                        }

                        Text(
                            text = if(msgText1.isEmpty()) "Send your first message" else msgText,
//                        color = if (chat.last?.read == true || chat.last?.senderId == state.userData?.userId) Color(170,170,170) else Color.White,
                            color = if(msgText1.isEmpty()) MaterialTheme.colorScheme.primary
                                else
                                (if (chat.last?.read == true || chat.last?.senderId == state.userData?.userId)
                                    MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground)
                            ,
                            style = if(msgText.isEmpty()) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W600)
                                else
                                (MaterialTheme.typography.titleSmall
                                    .copy(fontWeight = if(chat.last?.read == true || chat.last?.senderId == state.userData?.userId)
                                        FontWeight.Light else FontWeight.W500 ))

                            ,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )

                        val chatUserUnread = if(chat.user1?.userId == state.userData?.userId) chat.user1?.unread.toString() else chat.user2?.unread.toString()

//                        AnimatedVisibility(
//                            visible = chat.last?.read == false && chat.last?.senderId != state.userData?.userId && (chatUserUnread.toLong() > 0),
//                            enter = fadeIn(),
//                            exit = fadeOut()
//                        )
                        if(chat.last?.read == false && chat.last?.senderId != state.userData?.userId && (chatUserUnread.toLong() > 0))
                        {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
//                                    .padding(end = 8.dp)
                                ,
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                // Dot
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.95f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                )
                                {
                                    Text(text = chatUserUnread, color = MaterialTheme.colorScheme.primaryContainer, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

//                }
            }
        }
//        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var hasFocus by remember { mutableStateOf(false) }

    val animatedVerticalPadding by animateDpAsState(
        targetValue = if (hasFocus) 18.dp else 12.dp,
    )

    Row(modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            maxLines = 1,
            textStyle = androidx.compose.ui.text.TextStyle(
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
                    if (focusState.isFocused) hasFocus = true
                    else hasFocus = false
                    onQueryChange("")
                },
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = query,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true, // should be true for search
                    visualTransformation = VisualTransformation.None,
                    placeholder = {
                        Text(
                            text = "Search in chat",
                            fontSize = 16.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = colorScheme.surfaceContainerHigh,
                        disabledContainerColor = colorScheme.surfaceContainerHigh,
                        errorContainerColor = colorScheme.errorContainer,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    contentPadding = PaddingValues(vertical = animatedVerticalPadding, horizontal = 12.dp),
                    interactionSource = remember { MutableInteractionSource() },
                    leadingIcon = {
                        AnimatedVisibility(hasFocus) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .clip(CircleShape)
                                    .clickable { focusManager.clearFocus() }
                            )
                        }
                        AnimatedVisibility(!hasFocus) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                            )
                        }
                    }
                )
            }
        )
    }
}



