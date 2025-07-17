package com.utkarsh.chatapp.utilities

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.utkarsh.chatapp.Message
import androidx.core.net.toUri

fun downloadFileFromMessage(context: Context, message: Message) {
    val url: String
    val directory: String
    val fileType: String

    var fileName = message.content

    when {
        message.imageUrl.isNotEmpty() -> {
            url = message.imageUrl
            directory = Environment.DIRECTORY_PICTURES
            fileType = "photo"
            fileName = "Photo_${System.currentTimeMillis()}.jpg"
        }
        message.vidUrl.isNotEmpty() -> {
            url = message.vidUrl
            directory = Environment.DIRECTORY_PICTURES
            fileType = "video"
            fileName = "Video_${System.currentTimeMillis()}.mp4"
        }
        message.fileUrl.isNotEmpty() -> {
            url = message.fileUrl
            directory = Environment.DIRECTORY_DOWNLOADS
            fileType = "file"
        }
        else -> {
            Toast.makeText(context, "No file to download", Toast.LENGTH_SHORT).show()
            return
        }
    }

    try {
        val request = DownloadManager.Request(url.toUri()).apply {
            setTitle("Downloading $fileName")
            setDescription("Downloading $fileType...")

            if (fileType == "file") {
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            setDestinationInExternalPublicDir(directory, fileName)

            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        when (fileType) {
            "photo" -> Toast.makeText(context, "Saving photo to gallery", Toast.LENGTH_SHORT).show()
            "video" -> Toast.makeText(context, "Saving video to gallery", Toast.LENGTH_SHORT).show()
            "file" -> Toast.makeText(context, "Downloading file...", Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to download", Toast.LENGTH_SHORT).show()
    }
}