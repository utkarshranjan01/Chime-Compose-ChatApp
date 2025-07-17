package com.utkarsh.chatapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.utkarsh.chatapp.ChatViewModel
import com.utkarsh.chatapp.Message

@Composable
fun ReactionTab(
    onDismiss: () -> Unit,
    onReactionSelected: (String) -> Unit,
    message: Message,
    viewModel: ChatViewModel,
    userId: String,
    onClick: () -> Unit
) {

    val haptic = LocalHapticFeedback.current

    val userReaction = if(message.senderId == userId) message.reactSender else message.reactReceiver

    LazyRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
            .shadow(16.dp, shape = CircleShape)
            .width(300.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(listOf("👍", "❤️", "😂", "😮", "😢", "😡",
            "🔥", "💯", "🙌", "👏", "💀", "😭",
            "🤝", "🤔", "😍", "🥰", "😘", "🤗",
            "👀", "🤷", "🤦", "😎", "🤯", "🥺",
            "😤", "💪", "🙏", "✨", "⚡", "💎",
            "🎉", "🍀", "😌", "😆", "😬", "😅",
            "😇", "😈", "😋", "🤤", "🤩", "😴",
            "👋", "✌️", "👌", "🤌", "🫶", "🫠",
            "😵", "😵‍💫", "😓", "🥴", "🤢", "🤑",
            "😳", "😶", "😐", "🫣", "🫥", "🥹"),
            key = { emoji -> emoji }
            ) { emoji ->

            if (userReaction == emoji) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Text(
                        emoji,
                        fontSize = 24.sp,
                        modifier = Modifier
//                            .padding(horizontal = 6.dp)
                            .clickable {
                                onReactionSelected(emoji)
                                haptic.performHapticFeedback(
                                    HapticFeedbackType.Confirm
                                )
                                if (userReaction == emoji)
                                    viewModel.updateMessageReaction(message, "")
                                else
                                    viewModel.updateMessageReaction(message, emoji)
                                onClick()
                            }
                    )
                }
            } else {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                {
                    Text(
                        emoji,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable {
                                onReactionSelected(emoji)
                                haptic.performHapticFeedback(
                                    HapticFeedbackType.Confirm
                                )
                                if (userReaction == emoji)
                                    viewModel.updateMessageReaction(message, "")
                                else
                                    viewModel.updateMessageReaction(message, emoji)
                                onClick()
                            }
                    )
                }
            }
        }
    }
}
