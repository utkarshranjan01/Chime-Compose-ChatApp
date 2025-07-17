package com.utkarsh.chatapp.utilities

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeDisplay {


    // Chat UI

    @Composable
    fun LastSeenText2(lastSeenTimestamp: Long?) {
        var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

        // Tick every 1 minute to update
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000) // 1 minute
                currentTime = System.currentTimeMillis()
            }
        }

        val lastSeenText = remember(currentTime, lastSeenTimestamp) {
            formatLastSeenStyle2(lastSeenTimestamp)
        }

        AnimatedVisibility(visible = lastSeenTimestamp != null) {
//        Text(
//            text = lastSeenText,
//            modifier = Modifier.padding(start = 16.dp),
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp)
//        )
            Text(
                text = lastSeenText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }


    fun formatLastSeenStyle2(timestamp: Long?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = (diff / (60 * 1000)).toInt()
        val hours = (diff / (60 * 60 * 1000)).toInt()
        val days = (diff / (24 * 60 * 60 * 1000)).toInt()

        return when {
            minutes < 1 -> "Now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
//        days == 1 -> "Active yesterday"
            days <= 2 -> "${days}d"
            days <= 365 -> {
                val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
            else -> {
                val formatter = SimpleDateFormat("d/M/yy", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }



    // Chat Screen UI

    @Composable
    fun LastSeenText(lastSeenTimestamp: Long?, isOnlineVar: Boolean?) {
        var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

        // Tick every 1 minute to update
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000) // 1 minute
                currentTime = System.currentTimeMillis()
            }
        }

        val lastSeenText = remember(currentTime, lastSeenTimestamp) {
            formatLastSeenStyle(lastSeenTimestamp)
        }

        AnimatedVisibility(visible = lastSeenTimestamp != null && (isOnlineVar == null || !isOnlineVar)) {
            Text(
                text = lastSeenText,
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp)
            )
        }
    }


    fun formatLastSeenStyle(timestamp: Long?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val minutes = (diff / (60 * 1000)).toInt()
        val hours = (diff / (60 * 60 * 1000)).toInt()
        val days = (diff / (24 * 60 * 60 * 1000)).toInt()

        return when {
            minutes < 1 -> "Active recently"
            minutes < 60 -> "Active ${minutes}m ago"
            hours < 24 -> "Active ${hours}h ago"
            days == 1 -> "Active yesterday"
            days <= 7 -> "Active ${days} days ago"
            else -> {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "Active on ${formatter.format(Date(timestamp))}"
            }
        }
    }


    fun Date.toLocalDateOnly(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(this)
    }

    fun formatDate(dateString: String?): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString ?: "") ?: return ""

            val today = Calendar.getInstance()
            val givenDate = Calendar.getInstance().apply { time = date }

            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            return when {
                isSameDay(givenDate, today) -> "Today"
                isSameDay(givenDate, yesterday) -> "Yesterday"
                else -> {
                    val output = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                    output.format(date)
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}