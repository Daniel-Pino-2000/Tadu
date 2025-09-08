package com.myapp.tadu

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.myapp.tadu.notifications.CHANNEL_ID

class TaskListApp: Application() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Graph.provide(this)
    }

}