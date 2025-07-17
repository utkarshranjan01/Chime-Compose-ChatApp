package com.utkarsh.chatapp.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun DownloadDialog(
    onDismiss: ()-> Unit,
    onConfirm: ()-> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Download this file?")
        },
        text = {
            Text("This file will be saved in downloads folder.")
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}