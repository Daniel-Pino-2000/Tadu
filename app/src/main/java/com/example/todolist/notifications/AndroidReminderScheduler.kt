package com.example.todolist.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.example.todolist.data.Task

class AndroidReminderScheduler(
    private val context: Context

): ReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)


    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(task: Task) {

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("Task Title", task.title)
        }
        val reminderTime = task.reminder

        if (reminderTime != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                PendingIntent.getBroadcast(
                    context,
                    task.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    override fun cancel(task: Task) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                task.hashCode(),
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}