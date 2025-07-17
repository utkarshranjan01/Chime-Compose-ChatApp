package com.utkarsh.chatapp.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.widget.PopupMenu.OnDismissListener
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CropRotate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import android.net.Uri
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun StoryPreview(uri: Uri, hideDialog: () -> Unit, upload: (Uri, Context) -> Unit, context : Context) {

    var croppedUri: Uri? by remember {
        mutableStateOf(uri)
    }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        if (it.isSuccessful) {
            croppedUri = it.uriContent
        } else {
            var exception = it.error
        }
    }
    Dialog(
        onDismissRequest = { hideDialog.invoke() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
//            dismissOnClickOutside = false,

        )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
        ) {
            var scale by remember {
                mutableStateOf(1f)
            }
            var offset  by remember {
                mutableStateOf(Offset.Zero)
            }
            val brush = Brush.linearGradient(
                listOf(
//                    MaterialTheme.colorScheme.primary,
//                    MaterialTheme.colorScheme.secondary
                    Color(0xff238cdd),
                    Color(0xff1952c4),

                )
            )
            val brush2 = Brush.linearGradient(
                listOf(
//                    MaterialTheme.colorScheme.primary,
//                    MaterialTheme.colorScheme.secondary
                    Color(0xffa02424),
                    Color(0xffc43b56),
                    )
            )


            BoxWithConstraints {
                var state = rememberTransformableState {
                    zoomChange, panChange, rotationChange ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                    val extWidth = (scale-1)*constraints.maxWidth
                    val extHeight = (scale-1)*constraints.maxHeight
                    val maxX = (extWidth/2)
                    val maxY = (extHeight/2)
                    offset = Offset(
                        x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                        y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(0.8f)
//                            .fillMaxHeight(0.8f)
                            .padding(top = 64.dp) // Add padding for the crop button
                    ) {
                        AsyncImage(
                            model = if (croppedUri != null) croppedUri else uri,
                            contentDescription = "Cropped Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offset.x
                                    translationY = offset.y
                                }
                                .transformable(state)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f).padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        Button(onClick = hideDialog, modifier = Modifier.background(brush2, CircleShape), colors = ButtonDefaults.buttonColors(
                            Color.Transparent
                        )){
                            Text(text = "Cancel", color = Color.White)
                        }
                        Button(onClick = {
                            croppedUri?.let {
                                upload(it, context)
                                hideDialog()
                            }
                        }, modifier = Modifier.background(brush, CircleShape), colors = ButtonDefaults.buttonColors(
                            Color.Transparent
                        )){
                            Text(text = "Upload", color = Color.White)
                        }
                    }
                }

                IconButton(onClick = {
                    var cropOption = CropImageContractOptions(uri , CropImageOptions(activityBackgroundColor = Color(0xff000000).toArgb()))
                    launcher.launch(cropOption)
                }, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    Icon(imageVector = Icons.Rounded.CropRotate, contentDescription = null)
                }
            }
        }
    }
}