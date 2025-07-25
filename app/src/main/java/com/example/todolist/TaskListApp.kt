package com.example.todolist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.todolist.notifications.ReminderNotificationService

class TaskListApp:Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderNotificationService.TASK_CHANNEL_ID,
                "Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Used for the tasks reminders"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}