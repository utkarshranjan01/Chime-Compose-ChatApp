package com.utkarsh.chatapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.Message
import com.utkarsh.chatapp.utilities.TimeDisplay.formatDate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.utkarsh.chatapp.utilities.downloadFileFromMessage

@Composable
fun VideoPlayer(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = rememberExoPlayer(context, uri)

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = modifier
    )
}

@Composable
fun rememberExoPlayer(context: Context, uri: String): ExoPlayer {
    return remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
            prepare()
            playWhenReady = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreenUI(
    message: Message,
    onBack: () -> Unit,
    viewModel: ChatViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = viewModel.userMetadataCache[message.senderId]?.first ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatDate(message.time?.toDate()?.let {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                            }),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
//                        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            downloadFileFromMessage(context, message)
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            VideoPlayer(
                uri = message.vidUrl,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
//                    .aspectRatio(16 / 9f)
                    .align(Alignment.Center)
            )
        }
    }
}
