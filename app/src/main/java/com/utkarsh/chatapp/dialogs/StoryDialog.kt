package com.utkarsh.chatapp.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.utkarsh.chatapp.AppState
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.Story
import com.utkarsh.chatapp.utilities.TimeDisplay.formatLastSeenStyle
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun StoryDialog(
    appState: AppState,
    viewModel: ChatViewModel,
    story2: Story,
    hideDialog: () -> Unit,
) {
    val story = viewModel.stories.find { it.id == story2.id }?:return

    val pagerState = rememberPagerState(
        pageCount = { story.images.size },
        initialPage = 0,
        )

    val maxPageIndex = (story.images.size - 1).coerceAtLeast(0)

    val currentPage = pagerState.currentPage.coerceAtMost(maxPageIndex)

    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    val interactionSource = remember { MutableInteractionSource() }

    var showDeleteDialog by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = hideDialog, properties = DialogProperties(
                usePlatformDefaultWidth = false

            )
        ) {

            if(showDeleteDialog)
            {
                DeleteConfirmationDialog (
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        if(story.images.size <= 1) hideDialog
                        viewModel.deleteStoryImage(story.id, story.images[currentPage])
                    }
                )
            }



            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {

            }

            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
//                    .background(
//                    Color.White.copy(alpha = .6f),
//                    RoundedCornerShape(16.dp)
//                    ,
//
//                    )
//                    .border(1.dp, Color.White)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    IconButton(onClick = { hideDialog.invoke() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                    AsyncImage(
//                    model = story.ppurl,
                        model = viewModel.userMetadataCache[story.userId]?.third,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                    )
                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    {
                        Text(
                            text = if (story.userId == appState.userData?.userId) story.userName.toString() + " (You)" else story.userName.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = story.images[currentPage].time?.toDate()?.time.let {
                                formatLastSeenStyle(it).replace("Active on ", "").replace("Active ", "")
                                    .replace("yesterday", "Yesterday").replace("today", "Today")
                            }
//                        text = formatter.format(story.images[pagerState.currentPage].time?.toDate())
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    if (story.userId == appState.userData?.userId) {
                        IconButton(
                            onClick = {
                                showDeleteDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete, contentDescription = null
                            )
                        }
                    }
                }


                if(story.images.size > 1)
                {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,

                        )
                    {
                        repeat(story.images.size)
                        {
//                        val color = if (pagerState.currentPage == it) Color.DarkGray else Color.LightGray
                            val color =
                                if (currentPage == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            Box(
                                modifier = Modifier.padding(2.dp).height(5.dp).weight(1f).background(
                                    color, RoundedCornerShape(2.dp)
                                )
                            )
                            {

                            }
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(top = 64.dp)
//                .border(5.dp, Color.Yellow)
                    .fillMaxSize()
                    ,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {}
                            .fillMaxHeight(0.8f).fillMaxWidth(0.9f)
//                        .border(2.dp, MaterialTheme.colorScheme.primary,RoundedCornerShape(24.dp))
//                        .clip(shape = RoundedCornerShape(24.dp))
//                    .fillMaxSize()
                        ,
                        model = story.images[it].imgUrl,
                        contentDescription = "Current Story"
                    )
                }
            }
        }
}