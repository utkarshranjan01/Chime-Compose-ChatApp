import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

fun createHighImportanceNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "high_importance_channel", // Channel ID
        "Messages",                // Channel Name
        NotificationManager.IMPORTANCE_HIGH // Importance level
    ).apply {
        description = "Used for message notifications"
    }

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
