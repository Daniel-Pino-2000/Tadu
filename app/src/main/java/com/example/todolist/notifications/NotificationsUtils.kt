package com.example.todolist.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.todolist.R

private const val CHANNEL_NAME = "Task Reminders"
private const val CHANNEL_DESCRIPTION = "Reminders for your scheduled tasks"

fun showNotification(
    context: Context,
    taskTitle: String,
    taskDescription: String = "",
    reminderText: String = ""
) {
    try {
        // Permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationUtils", "POST_NOTIFICATIONS permission not granted")
            return
        }

        // Check if notifications are enabled for the app
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.w("NotificationUtils", "Notifications are disabled for the app")
            return
        }

        // Create notification channel if needed (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Create intent to open the app when notification is tapped
        val intent = try {
            Intent(context, Class.forName("com.example.todolist.MainActivity")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // You can add extras here to navigate to specific task if needed
                // putExtra("TASK_ID", taskId)
            }
        } catch (e: ClassNotFoundException) {
            Log.e("NotificationUtils", "MainActivity class not found", e)
            return
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prepare notification content
        val contentText = when {
            reminderText.isNotBlank() -> reminderText
            taskDescription.isNotBlank() -> "Don't forget: $taskDescription"
            else -> "You set a reminder for \"$taskTitle\". Don't forget to complete it!"
        }

        // Build and show notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Consider using a better icon like R.drawable.ic_notification
            .setContentTitle("Task Reminder")
            .setContentText("$taskTitle")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
            )
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        // Use a unique notification ID based on current time to avoid overwriting
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        Log.d("NotificationUtils", "Notification displayed with ID: $notificationId for task: $taskTitle")

    } catch (e: SecurityException) {
        Log.e("NotificationUtils", "Security exception when showing notification", e)
    } catch (e: Exception) {
        Log.e("NotificationUtils", "Error showing notification for task: $taskTitle", e)
    }
}

/**
 * Check if the app can show notifications (considers both system permission and app-level settings)
 */
fun canShowNotifications(context: Context): Boolean {
    // Check system permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }

    // Check if notifications are enabled at the app level
    val notificationManager = NotificationManagerCompat.from(context)
    return notificationManager.areNotificationsEnabled()
}