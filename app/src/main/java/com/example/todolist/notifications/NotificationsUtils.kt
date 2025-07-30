package com.example.todolist.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todolist.R

fun showNotification(context: Context, title: String) {
    val channelId = "reminder_channel"
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher) // or R.drawable.ic_notification
        .setContentTitle(title)
        .setContentText("Reminder triggered!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    manager.notify(System.currentTimeMillis().toInt(), notification)
}
