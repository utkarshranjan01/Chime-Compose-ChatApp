package com.utkarsh.chatapp.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import android.widget.Toast
import androidx.compose.ui.platform.LocalWindowInfo
import com.utkarsh.chatapp.AppState
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.NavigationState
import com.utkarsh.chatapp.R
import com.utkarsh.chatapp.dialogs.StoryPreview
import kotlinx.coroutines.delay


//@Preview(
//    showSystemUi = true,
////    uiMode = Configuration.UI_MODE_NIGHT_NO,
//    showBackground = true,
////    name = "Dark Mode"
//)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeInfoPage(viewModel: ChatViewModel, state: AppState, onGetStartedClick: () -> Unit, onSignOutClick: () -> Unit, navController: NavController, hasCompletedSetup: Boolean) {

//    fun ChangeInfoPage() {


    val userData = state.userData
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textState = remember { mutableStateOf(TextFieldValue()) }
    val textState2 = remember { mutableStateOf(TextFieldValue()) }
    val usernameState = remember { mutableStateOf("") }
    val bioState = remember { mutableStateOf("") }


    var usernameError by remember { mutableStateOf<String?>(null) }

    fun validateAndUpdate() {
        if (textState.value.text.trim().isEmpty()) {
            usernameError = "Name cannot be empty"
            return
        }

        usernameError = null
        viewModel.updateUserProfile(
            username = textState.value.text.trim(),
            bio = textState2.value.text.trim()
        )
        NavigationState.currentRoute = "ChatsScreen"
        onGetStartedClick()
    }

    val context = LocalContext.current

    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        addCategory(Intent.CATEGORY_OPENABLE) // only show openable files
    }
    val chooser = Intent.createChooser(intent, "Select a Photo")

    var selectedUriForPreview by remember { mutableStateOf<Uri?>(null) }

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


    var isVisible by remember { mutableStateOf(true) }

//    val alpha by animateFloatAsState(
//        targetValue = if (isVisible) 1f else 0f, // Animate opacity
//        animationSpec = tween(durationMillis = 500), label = "" // Animation duration
//    )

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(500) // Match the duration of the animation
            onSignOutClick()
        }
    }


    LaunchedEffect(state.userData) {
        state.userData?.username?.let { username ->
            textState.value = TextFieldValue(username)
            usernameState.value = username
        }
        state.userData?.bio?.let { username ->
            textState2.value = TextFieldValue(username)
            bioState.value = username
        }
    }


    val focusRequester = remember { FocusRequester() }

        AnimatedVisibility(
            visible = isVisible,
            exit = fadeOut(animationSpec = tween(durationMillis = 500)) // Fade-out animation
        )
        {

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            state.userData?.email?.let {
                                Text(
                                    it, maxLines = 1, // Restrict to a single line
                                    overflow = TextOverflow.Ellipsis // Add ellipsis for overflow
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
//                            textState.value = TextFieldValue(usernameState.value)
//                            textState2.value = TextFieldValue(bioState.value)
                                state.userData?.username?.let { username ->
                                    textState.value = TextFieldValue(username)
                                    usernameState.value = username
                                }
                                state.userData?.bio?.let { username ->
                                    textState2.value = TextFieldValue(username)
                                    bioState.value = username
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Reset"
                                )
                            }
                        }
                    )
                },
//                    bottomBar = {
//                        if(hasCompletedSetup)
//                        {
//                            BottomNavigationBar(navController = navController)
//                        }
//                    }
            ) { innerPadding ->


                Box(
                    modifier = Modifier.padding(innerPadding)
                )
                {
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .imePadding()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        // Dismiss focus and hide keyboard when tapping outside the text field
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                )
                            }
                    ) {
                        items(count = 1)
                        {

                            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                            // You can cover this box inside userData?.ppurl?.let {} so that if user or ppurl is null its not showing but not adding that as its getting the information

                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(
                                        top = screenHeight * 0.04f,
                                        bottom = screenHeight * 0.04f
                                    ),
                                contentAlignment = Alignment.Center, // Centers both horizontally & vertically

                            ) {
                                AsyncImage(
                                    // this loads image from the internet

                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(userData?.ppurl)
                                        .crossfade(false)
                                        .allowHardware(true)
                                        .build(),
//                                    placeholder = painterResource(R.drawable.person_placeholder_4),
                                    error = painterResource(R.drawable.person_placeholder_4),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
//                            modifier = Modifier.size(100.dp).clip(CircleShape)
                                    modifier = Modifier.fillMaxWidth(0.4f).aspectRatio(1f)
                                        .clip(CircleShape).clickable {

                                        }

                                )
                                IconButton(
                                    onClick = {
                                        launcher.launch(chooser)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
//                                .offset(x = (-).dp, y = (-8).dp)
                                        .offset(
                                            x = -(screenWidth * 0.3f),
                                            y = (-4).dp
                                        )  // Adjust x offset based on screen width
//                                .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
//                                modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
//                        text = state.userData?.username.toString(),
                                text = usernameState.value,
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis

//                        fontWeight = FontWeight.Bold

                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = bioState.value,
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis

                            )
                            Spacer(modifier = Modifier.height(24.dp))
//                    Text(
//                        text = "Name",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 16.dp, bottom = 8.dp),
//                        style = MaterialTheme.typography.bodyLarge,
//                    )
                            OutlinedTextField(
                                value = textState.value,
                                label = { Text("Name") },
                                isError = usernameError != null,
                                supportingText = usernameError?.let {
                                    { Text(it, color = MaterialTheme.colorScheme.error) }
                                },
                                onValueChange = {
                                    textState.value = it
                                    usernameState.value = it.text
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp)
                                    .focusRequester(focusRequester),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, // Shows "Next" button
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Focus the next field when "Next" is pressed
                                        focusRequester.requestFocus()
                                    }
                                )
//                        colors = TextFieldDefaults.colors(
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent
//                        )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
//                    Text(
//                        text = "Bio",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(start = 16.dp, bottom = 8.dp),
//                        style = MaterialTheme.typography.bodyLarge,
//                    )

                            OutlinedTextField(
                                value = textState2.value,
                                label = { Text("Bio") },
                                onValueChange = {
                                    textState2.value = it
                                    bioState.value = it.text
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp)
                                    .focusRequester(focusRequester),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done, // Done button for the last field
                                    capitalization = KeyboardCapitalization.Sentences
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            TextButton(onClick = {
//                        navController.navigate("signup")
                                isVisible = false
//                                onSignOutClick()
                            }) {
                                Text(text = "Don't want to use this account")
                            }
                            Button(
                                onClick = {
//                                    onGetStartedClick()
                                    validateAndUpdate()
                                },
                                modifier = Modifier
//                            .fillMaxWidth()
                                    .padding(16.dp),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Text(
                                    text = if (hasCompletedSetup) "Save" else "Get Started",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                selectedUriForPreview?.let { uri ->
                    StoryPreview(
                        uri = uri,
                        context = context,
                        hideDialog = { selectedUriForPreview = null },
                        upload = { uploadedUri, _ ->
                            viewModel.uploadFile(uploadedUri, context) {
                                viewModel.updateProfile(
                                    it,
                                    state.userData?.userId.orEmpty(),
                                    context
                                )
                            }
                            selectedUriForPreview = null
                        }
                    )
                }


            }


        }
}
