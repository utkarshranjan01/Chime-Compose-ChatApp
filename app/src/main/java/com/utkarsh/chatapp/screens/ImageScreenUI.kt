package com.utkarsh.chatapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.SubcomposeAsyncImage
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.Message
import com.utkarsh.chatapp.utilities.TimeDisplay.formatDate
import com.utkarsh.chatapp.utilities.downloadFileFromMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreenUI(
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
                            text = viewModel.userMetadataCache[message.senderId]?.first?:"",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatDate((message.time)?.toDate()?.let {
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

        Log.d("T15", message.senderName)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = message.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                loading = { CircularProgressIndicator() }
            )
        }
    }
}
