package com.utkarsh.chatapp.screens

import android.content.Intent
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.utkarsh.chatapp.AppState
import com.utkarsh.chatapp.ChangeInfoPage
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.NavigationState
import com.utkarsh.chatapp.R
import com.utkarsh.chatapp.Story
import com.utkarsh.chatapp.utilities.TimeDisplay.formatLastSeenStyle
import com.utkarsh.chatapp.dialogs.StoryDialog
import com.utkarsh.chatapp.dialogs.StoryPreview

@Composable
fun StatusScreen(
    navController: NavController,
    viewModel: ChatViewModel = ChatViewModel(),
    state: AppState = AppState()
) {

    var border = Brush.sweepGradient(
        listOf(
            Color(0xFFa7e6FF),
            Color(0xFFa7e6FF),
        )
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    val lottieAnimationFile by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.upload)
    )

    val myStoryVm = viewModel.stories.filter {
        it.userId == state.userData?.userId
    }
    val myStory = myStoryVm.ifEmpty { // it will have only the current user story
        listOf(Story()) // if empty then we just pass a empty list
    }

    val stories = viewModel.stories.filter { // it will have stories of other users
        it.userId != state.userData?.userId
    }
    var showStory by remember { mutableStateOf(false) }
    var curStory by remember { mutableStateOf(Story()) }

    var selectedUriForPreview by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        addCategory(Intent.CATEGORY_OPENABLE) // only show openable files
    }
    val chooser = Intent.createChooser(intent, "Select a Photo")

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->

            val uri = result.data?.data

            uri?.let {
                val type = context.contentResolver.getType(it)

                val fileSize = context.contentResolver.openFileDescriptor(it, "r")?.use { pfd ->
                    pfd.statSize
                } ?: -1

                val maxSize = 25 * 1024 * 1024L  // Max file size is 25MB
                if (fileSize > maxSize) {
                    Toast.makeText(
                        context,
                        "File is too large! Max 25 MB allowed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@let
                }
                selectedUriForPreview = it
            }

        }

    val selectedItem = remember { mutableStateListOf<String?>() }

    var isUploading by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar =
                {
                    Box(
                        modifier = Modifier.statusBarsPadding()
//                        modifier = Modifier.padding(it)
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
                        ) {
                            Text(
                                text = "Stories",
//                                modifier = Modifier.padding(start = 16.dp).offset(y = 5.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
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
                            Column{
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
//                                        error = painterResource(R.drawable.person_placeholder_4),
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
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(
                                            text = "Profile",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
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
                                        },
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                            }
                        }
                    }
                },
            bottomBar =
                {
                    NavigationBar {}
                    // to add padding at the bottom of screen
                }
        ) {innerPadding ->

            Column(
                modifier = Modifier.padding(innerPadding)
            )
            {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
                )
                {

                    item {

                        if (isUploading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            )
                            {
                                Box(
                                    modifier = Modifier
                                        .size(77.dp).padding(3.dp),
                                    contentAlignment = Alignment.Center
                                )
                                {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(viewModel.userMetadataCache[state.userData?.userId]?.third)
//                                        .data(state.userData?.ppurl)
                                            .crossfade(true)
                                            .allowHardware(true)
                                            .build(),
//                                    error = painterResource(id = R.drawable.person_placeholder_4),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .fillMaxSize()
                                    )

                                    LottieAnimation(
                                        composition = lottieAnimationFile,
                                        iterations = LottieConstants.IterateForever,
                                        modifier = Modifier.background(
                                            Color.Black.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                    )

                                }
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                {
                                    Text(
                                        text = "Uploading...",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = myStory[0].images.lastOrNull()?.time?.toDate()?.time?.let {
                                            formatLastSeenStyle(it).replace("Active on", "Posted")
                                                .replace("Active", "Posted")
                                                .replace("yesterday", "Yesterday")
                                        } ?: "Share your stories",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                            }
                        }
                        // basically this is checking the 0th index of the current user story so to find if the user has uploaded any story or not
                        if (myStory[0].userId.isNotEmpty() && !isUploading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    showStory = true
                                    curStory = myStory[0]
                                }.fillMaxWidth()
                            )
                            {
                                Box(
                                    modifier = Modifier
                                        .size(77.dp)
                                        .padding(3.dp),
                                    contentAlignment = Alignment.BottomEnd
                                )
                                {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
//                                        .data(viewModel.userMetadataCache[myStory[0].userId]?.third)
                                            .data(myStory.last().images.last().imgUrl)
                                            .crossfade(true).allowHardware(true).build(),
//                                    placeholder = painterResource(id = R.drawable.person_placeholder_4),
                                        contentDescription = null,
//                                    error = painterResource(id = R.drawable.person_placeholder_4),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .fillMaxSize().border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(30.dp).clickable {
                                            launcher.launch(chooser)
                                        }
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .size(32.dp)
                                            .padding(4.dp)
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                {
                                    Text(
                                        text = "Your Posts",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = formatLastSeenStyle(myStory[0].images.last().time?.toDate()?.time)
                                            .replace("Active on", "Posted")
                                            .replace("Active", "Posted")
                                            .replace("yesterday", "Yesterday"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                        if (myStory[0].userId.isEmpty() && !isUploading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    launcher.launch(chooser)
                                }.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
//                                    .padding(bottom = 20.dp, start = 5.dp, end = 5.dp, top = 10.dp)
                                        .size(77.dp).padding(3.dp)
                                        .drawWithCache {
                                            onDrawBehind {
                                                drawCircle(
                                                    brush = SolidColor(primaryColor),
//                                                brush = border,
                                                    style = Stroke(
                                                        width = 8f,
                                                        pathEffect = PathEffect.dashPathEffect(
                                                            floatArrayOf(
                                                                (35.dp.toPx() * 2 * Math.PI.toFloat() / 5) - 15f,
                                                                15f
                                                            ), 0f
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                                                alpha = .4f
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                )
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = Color(MaterialTheme.colorScheme.primary.value).copy(
                                            alpha = .8f
                                        ),
                                        modifier = Modifier.size(40.dp)
                                    )

                                }
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                {
                                    Text(
                                        text = "Your Posts",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "Share your stories",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recent Stories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )
                    }

                    items(stories, key = { story -> story.userId }) { story ->
                        StoryItem(
                            story = story,
                            viewModel = viewModel,
                            myStory = myStory,
                            onClick = {
                                curStory = story
                                showStory = true
                            }
                        )

                    }
                }
            }

            // This displays when no chats exist or you search something which has no result
            if(stories.isEmpty() && viewModel.isStoryInitialized)
            {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 300.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Text("No posts from you friends", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(32.dp))
                    Image(
                        painter = painterResource(id = R.drawable._023449),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Empty Status Screen",
                        modifier = Modifier.clip(RoundedCornerShape(24.dp)).size(300.dp)
                    )
                }
            }

            AnimatedVisibility(showStory,
//                enter = fadeIn() + expandIn(expandFrom = Alignment.Center), // Entry animation
//                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center) // Exit animation)
            )
            {
                StoryDialog(
                 appState = state,
                    viewModel = viewModel,
                    story2 = curStory,
                    hideDialog = {
                        showStory = !showStory
                        selectedItem.clear()
                    }
                )
            }


            selectedUriForPreview?.let { uri ->
                StoryPreview(
                    uri = uri,
                    context = context,
                    hideDialog = { selectedUriForPreview = null },
                    upload = { uploadUri, context ->
                        isUploading = true
                        viewModel.uploadFile(uploadUri, context) { url ->
                            viewModel.uploadStory(
                                url,
                                myStory[0].id
                            )
                            isUploading = false
                        }
                        selectedUriForPreview = null
                    }
                )
            }
        }
}
@Composable
fun StoryItem(
    story: Story,
    viewModel: ChatViewModel,
    myStory: List<Story>,
    onClick: (Story) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp).clickable { onClick(story) }.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(77.dp)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
//                    .data(viewModel.userMetadataCache[story.userId]?.third)
                    .data(story.images.last().imgUrl)
                    .crossfade(true)
                    .allowHardware(true)
                    .build(),
//                placeholder = painterResource(R.drawable.person_placeholder_4),
//                error = painterResource(R.drawable.person_placeholder_4),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize().border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = viewModel.userMetadataCache[story.userId]?.first.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.W600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatLastSeenStyle(story.images.lastOrNull()?.time?.toDate()?.time)
                    .replace("Active on", "Posted").replace("Active", "Posted")
                    .replace("yesterday", "Yesterday").replace("today", "Today"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
