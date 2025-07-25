package com.example.todolist.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.todolist.data.Task
import androidx.core.app.NotificationCompat
import com.example.todolist.MainActivity
import com.example.todolist.R

class ReminderNotificationService(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(task: Task) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(context, 1, activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val checkIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, ReminderNotificationReceiver::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Task Reminder")
            .setContentText("You have not finished with ${task.title}")
            .setContentIntent(activityPendingIntent)
            .build()
            /*
            .addAction(
                R.drawable.baseline_check_24,
                "Mark as Done",
                checkIntent
            )

            //.setStyle() for the future

             */
        notificationManager.notify(1, notification)
    }

    companion object {
        const val TASK_CHANNEL_ID = "task_channel"
    }
}