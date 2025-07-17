package com.utkarsh.chatapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import com.utkarsh.chatapp.googleSign.GoogleAuthUiClient
import com.utkarsh.chatapp.ui.theme.ChatAppTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.utkarsh.chatapp.utilities.FCMTokenManager
import com.utkarsh.chatapp.utilities.rememberHasCompletedSetup
import com.utkarsh.chatapp.screens.ChangeInfoPage
import com.utkarsh.chatapp.screens.ChatScreenUI
import com.utkarsh.chatapp.screens.ChatUI
import com.utkarsh.chatapp.screens.SignInScreenUI
import com.utkarsh.chatapp.screens.StatusScreen

import com.utkarsh.chatapp.utilities.HAS_COMPLETED_SETUP_KEY
import com.utkarsh.chatapp.utilities.dataStore
import com.utkarsh.chatapp.screens.ImageScreenUI
import com.utkarsh.chatapp.screens.VideoScreenUI
import com.utkarsh.chatapp.utils.PresenceManager
import com.utkarsh.chatapp.utils.PresenceManager.TrackPresence
import createHighImportanceNotificationChannel

class MainActivity : ComponentActivity() {

    @Stable
    object NavigationState {
        var currentRoute by mutableStateOf("NotDone")
    }

    private val viewModel: ChatViewModel by viewModels()

    var initialValue = ""

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            viewModel = viewModel,
            oneTapClient = Identity.getSignInClient((applicationContext))
        )
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ContextCastToActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // To display the banner in the notification
        createHighImportanceNotificationChannel(this)

        val sharedPrefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val hasCompletedSetup2 = sharedPrefs.getBoolean("COMPLETED_SETUP", false)


        setContent {

            ChatAppTheme {
                val hasCompletedSetup = rememberHasCompletedSetup()
                val state by viewModel.state.collectAsState()
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (NavigationState.currentRoute!="NotDone" &&
                            NavigationState.currentRoute!="ChatScreen" &&
                            NavigationState.currentRoute!="SignInScreen" &&
                            NavigationState.currentRoute!="ImageScreen" &&
                            NavigationState.currentRoute!="VideoScreen") {
                            BottomNavigationBar(navController = navController)
                        }
                    }

                ) { innerPadding ->

                        NavHost(navController = navController, startDestination = StartScreen) {

                            composable<StartScreen>
                            {

                                NavigationState.currentRoute="NotDone"
                                // this would not be having an screen it will just check if we have user data
                                LaunchedEffect(key1 = Unit)
                                {
                                    val userData = googleAuthUiClient.getSignedInUser()

                                    if (userData != null) {

                                        initialValue = userData.userId.toString()

                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)

                                        // This is to display stories basically sees all the chats the user is part of then shows the stories from those users
                                        viewModel.popStory(userData.userId)

                                        // This to add the metadata of current user to show profile icon
                                        viewModel.fetchUserMetadata(userData.userId.toString())

                                        if (!hasCompletedSetup2) { // Initial setup for username and profile picture
                                            NavigationState.currentRoute="NotDone"
                                            navController.navigate(ChangeInfoPage)
                                        } else {
                                            navController.navigate(ChatsScreen)
                                        }
                                    } else {
                                        navController.navigate(SignInScreen)
                                    }
                                }


                            }

                            composable<SignInScreen>(
                                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 300, easing = LinearEasing)) }, // Very fast fade-in
                                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 300, easing = LinearEasing)) }, // Very fast fade-out
                                popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = 300, easing = LinearEasing)) },
                                popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 300, easing = LinearEasing)) }
                            )
                            {

                                NavigationState.currentRoute="SignInScreen"

                                val activity = LocalContext.current as? Activity

                                BackHandler(enabled = true) {
                                    activity?.let {
                                        ActivityCompat.finishAffinity(it) // Closes all activities and exits the app
                                    }
                                }

                                val isSigningIn by viewModel.isSigningIn.collectAsState()

                                val launcher =
                                    rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult(),
                                        onResult = { result ->
                                            if (result.resultCode == RESULT_OK) {
                                                lifecycleScope.launch {
                                                    val signInResult =
                                                        googleAuthUiClient.signInWithIntent(
                                                            intent = result.data
                                                                ?: return@launch
                                                        )
                                                    viewModel.onSignInResult(signInResult)
                                                    Toast.makeText(activity, "Sign In Successful", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                viewModel.setSigningIn(false) // If sign in fails the sign in button is enabled again
                                            }
                                        })

                                LaunchedEffect(key1 = state.isSignedIn) {
                                    val userData = googleAuthUiClient.getSignedInUser()
                                    userData?.run {
                                        viewModel.adduserToFirestore(userData)
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)

                                        viewModel.popStory(userData.userId)

                                        navController.navigate(ChangeInfoPage)

                                    }
                                }

                                SignInScreenUI(
                                    onSignInClick = {
                                        if (!isSigningIn) {
                                            lifecycleScope.launch {
                                                val signInIntentSender = googleAuthUiClient.signIn()
                                                launcher.launch(
                                                    IntentSenderRequest.Builder(
                                                        signInIntentSender ?: return@launch
                                                    ).build()
                                                )
                                                viewModel.setSigningIn(true)
                                            }
                                        }
                                    },
                                    isSigningIn = isSigningIn
                                )
                            }

                            composable<ChangeInfoPage>(
                                enterTransition = {
                                    scaleIn(animationSpec = tween(durationMillis = 220, easing = EaseOut), initialScale = 0.92f) + fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))
                                },
                                exitTransition = {
                                    fadeOut(animationSpec = tween(durationMillis = 150, easing = EaseOut))
                                },
                                popEnterTransition = {
                                    scaleIn(animationSpec = tween(durationMillis = 220, easing = EaseOut), initialScale = 0.92f) + fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))
                                },
                                popExitTransition = {
                                    fadeOut(animationSpec = tween(durationMillis = 150, easing = EaseOut))
                                }
                            )
                            {

                                val onSignOutClick2: () -> Unit = {

                                    NavigationState.currentRoute="NotDone"
                                    lifecycleScope.launch {
                                        sharedPrefs.edit().putBoolean("COMPLETED_SETUP", false)
                                            .apply()
                                        dataStore.edit { preferences ->
                                            preferences[HAS_COMPLETED_SETUP_KEY] = false
                                        }
                                        googleAuthUiClient.signOut()
                                        viewModel.resetState()
                                        Toast.makeText(applicationContext,"Sign Out", Toast.LENGTH_SHORT).show()
                                        navController.navigate(SignInScreen) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive =
                                                    true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                }



                                BackHandler(enabled = true) {
                                    if (!hasCompletedSetup) {
                                        onSignOutClick2()
                                    }
                                    else
                                    {
                                        com.utkarsh.chatapp.NavigationState.currentRoute = "ChatsScreen"
                                        navController.navigate(ChatsScreen) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }

                                val userData = googleAuthUiClient.getSignedInUser()
                                ChangeInfoPage(
                                    viewModel = viewModel,
                                    state = state,
                                    onGetStartedClick = {
                                        NavigationState.currentRoute="ChangeInfoPage"
                                        sharedPrefs.edit().putBoolean("COMPLETED_SETUP", true)
                                            .apply()
                                        lifecycleScope.launch {
                                            // Update DataStore to true
                                            dataStore.edit { preferences ->
                                                preferences[HAS_COMPLETED_SETUP_KEY] = true
                                            }
                                        }
//                                        navController.navigate(ChatsScreen)
                                        navController.navigate(ChatsScreen) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive =
                                                    true // Crucial: Include the start destination
                                            }
                                            launchSingleTop = true
                                        }


                                    },
                                    onSignOutClick = onSignOutClick2,
                                    navController = navController,
                                    hasCompletedSetup = hasCompletedSetup
                                )
                            }

                            composable<ChatsScreen>(

                                enterTransition = {
                                    scaleIn(animationSpec = tween(durationMillis = 220, easing = EaseOut), initialScale = 0.92f) + fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))
                                },
                                exitTransition = {
                                    fadeOut(animationSpec = tween(durationMillis = 150, easing = EaseOut))
                                },
                                popEnterTransition = {
                                    scaleIn(animationSpec = tween(durationMillis = 220, easing = EaseOut), initialScale = 0.92f) + fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))
                                },
                                popExitTransition = {
                                    fadeOut(animationSpec = tween(durationMillis = 150, easing = EaseOut))
                                }
                            )
                            {


                                FCMTokenManager.uploadToken()
                                NavigationState.currentRoute="ChatsScreen"

                                viewModel.hasLoadedInitialMessages = false

                                TrackPresence(initialValue)

                                val activity = LocalContext.current as? Activity
                                BackHandler(enabled = true) {
                                    activity?.finish()
                                }

                                ChatScreenUI(
                                    viewModel = viewModel,
                                    state = state,
                                    showSingleChat = { usr, id ->
                                        viewModel.getTp(id)
                                        viewModel.setChatUser(usr, id)
                                        navController.navigate(ChatScreen)
                                    },
                                    navController = navController
                                )
                            }

                            composable<ChatScreen>(

                                enterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                        initialAlpha = 0.3f // Subtle fade
                                    )
                                },
                                exitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -fullWidth },
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(
                                        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                                    )
                                },
                                popEnterTransition = {
                                    slideInHorizontally(
                                        initialOffsetX = { fullWidth -> -fullWidth },
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
                                        initialAlpha = 0.3f
                                    )
                                },
                                popExitTransition = {
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(
                                        animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
                                    )
                                }

                            )
                            {

                                NavigationState.currentRoute="ChatScreen"

                                viewModel.isLoadingMore = false

                                ChatUI(
                                    viewModel = viewModel,
                                    navController = navController,
                                    // below change back to messages if not want to use preload
//                                    messages2 = viewModel.allMessages,
                                    userData = state.User2!!,
                                    chatId = state.chatId,
                                    state = state,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable<ImageScreen>(
                                enterTransition = {
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(durationMillis = 220, easing = EaseOut)
                                    )
                                },
                                exitTransition = {
                                    fadeOut(
                                        animationSpec = tween(durationMillis = 150, easing = EaseOut)
                                    )
                                },
                                popEnterTransition = {
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(durationMillis = 220, easing = EaseOut)
                                    )
                                },
                                popExitTransition = {
                                    fadeOut(
                                        animationSpec = tween(durationMillis = 150, easing = EaseOut)
                                    )
                                }

                            )
                            {

                                NavigationState.currentRoute="ImageScreen"

                                val message = viewModel.currentImageMessage

                                if (message != null) {
                                    ImageScreenUI(
                                        message = message,
                                        onBack = { navController.popBackStack() },
                                        viewModel = viewModel
                                    )
                                }

                            }

                            composable<VideoScreen>(
                                enterTransition = {
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(durationMillis = 220, easing = EaseOut)
                                    )
                                },
                                exitTransition = {
                                    fadeOut(
                                        animationSpec = tween(durationMillis = 150, easing = EaseOut)
                                    )
                                },
                                popEnterTransition = {
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(durationMillis = 220, easing = EaseOut)
                                    )
                                },
                                popExitTransition = {
                                    fadeOut(
                                        animationSpec = tween(durationMillis = 150, easing = EaseOut)
                                    )
                                }

                            )
                            {

                                NavigationState.currentRoute="VideoScreen"

                                // This variable is used for both images and videos
                                val message = viewModel.currentImageMessage

                                if (message != null) {
                                    VideoScreenUI(
                                        message = message,
                                        onBack = { navController.popBackStack() },
                                        viewModel = viewModel
                                    )
                                }

                            }



                            composable<StatusPage>(
                                enterTransition = {
                                    scaleIn(animationSpec = tween(durationMillis = 220, easing = EaseOut), initialScale = 0.92f) + fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))
                                },
                                exitTransition = {
                                    fadeOut(animationSpec = tween(durationMillis = 150, easing = EaseOut))
                                },
                                popEnterTransition = {
                                    scaleIn(animationSpec = tween(durationMillis = 220, easing = EaseOut), initialScale = 0.92f) + fadeIn(animationSpec = tween(durationMillis = 220, easing = EaseOut))
                                },
                                popExitTransition = {
                                    fadeOut(animationSpec = tween(durationMillis = 150, easing = EaseOut))
                                }
                            )
                            {

                                BackHandler(enabled = true) {
                                    com.utkarsh.chatapp.NavigationState.currentRoute = "ChatsScreen"
                                    navController.navigate(ChatsScreen) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                NavigationState.currentRoute="StatusScreen"
                                StatusScreen(navController = navController, viewModel = viewModel, state = state)
                            }


                        }
//                    }


                }
            }
        }


    }
}