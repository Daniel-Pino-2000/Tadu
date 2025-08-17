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
                setShowBadge(true)
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity when tapped
        val intent = try {
            Intent(context, Class.forName("com.example.todolist.MainActivity")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        // Improved notification text content
        val (notificationTitle, contentText, expandedText) = buildNotificationText(
            taskTitle, taskDescription, reminderText
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Use dedicated notification icon
            .setContentTitle(notificationTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Better than manual vibration
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .build()

        // Use unique ID to avoid overwriting other notifications
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        Log.d(
            "NotificationUtils",
            "Notification displayed with ID: $notificationId for task: $taskTitle"
        )

    } catch (e: SecurityException) {
        Log.e("NotificationUtils", "Security exception when showing notification", e)
    } catch (e: Exception) {
        Log.e("NotificationUtils", "Error showing notification for task: $taskTitle", e)
    }
}

/**
 * Builds well-formatted notification text following Material Design guidelines.
 * Returns Triple of (title, contentText, expandedText)
 */
private fun buildNotificationText(
    taskTitle: String,
    taskDescription: String,
    reminderText: String
): Triple<String, String, String> {
    // Title should clearly indicate it's a reminder
    val title = "Task Reminder: $taskTitle"

    // Content text for collapsed notification (should be under 40 chars when possible)
    val contentText = when {

        taskDescription.isNotBlank() -> taskDescription.take(50).let {
            if (taskDescription.length > 50) "$it..." else it
        }

        reminderText.isNotBlank() -> reminderText.take(50).let {
            if (reminderText.length > 50) "$it..." else it
        }

        else -> "Don't forget to complete this task"
    }

    // Expanded text for BigTextStyle (can be longer and more detailed)
    val expandedText = buildString {

        if (taskDescription.isNotBlank()) {
            append("\nDescription: $taskDescription")
        }

        if (reminderText.isNotBlank()) {
            append("\n\nReminder: $reminderText")
        }

        append("\n\nTap to open your to-do list")
    }

    return Triple(title, contentText, expandedText)
}

/**
 * Check if the app can show notifications (system + app-level settings).
 */
fun canShowNotifications(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }

    val notificationManager = NotificationManagerCompat.from(context)
    return notificationManager.areNotificationsEnabled()
}