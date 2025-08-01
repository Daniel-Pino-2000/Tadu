package com.example.todolist.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.todolist.data.Task

class AndroidReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(task: Task) {
        // Only schedule if reminder time is set and in the future
        val reminderTime = task.reminderTime
        if (reminderTime == null || reminderTime <= System.currentTimeMillis()) {
            Log.d("ReminderScheduler", "Skipping reminder for task ${task.id}: invalid time")
            return
        }

        // Check permissions
        if (!hasRequiredPermissions()) {
            Log.w("ReminderScheduler", "Missing required permissions for scheduling exact alarms")
            return
        }

        try {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("TASK_ID", task.id)
                putExtra("TASK_TITLE", task.title)
                putExtra("TASK_DESCRIPTION", task.description)
                putExtra("REMINDER_TEXT", task.reminderText)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use appropriate alarm method based on Android version and permissions
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                    Log.d("ReminderScheduler", "Scheduled exact alarm for task ${task.id} at $reminderTime")
                }
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                    Log.d("ReminderScheduler", "Scheduled exact alarm for task ${task.id} at $reminderTime (pre-S)")
                }
                else -> {
                    // Fallback to inexact alarm if exact alarms aren't allowed
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                    Log.d("ReminderScheduler", "Scheduled inexact alarm for task ${task.id} at $reminderTime")
                }
            }

        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Security exception when scheduling alarm for task ${task.id}", e)
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error scheduling alarm for task ${task.id}", e)
        }
    }

    override fun cancel(task: Task) {
        try {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel() // Also cancel the PendingIntent
            Log.d("ReminderScheduler", "Cancelled alarm for task ${task.id}")

        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error cancelling alarm for task ${task.id}", e)
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> true // No special permission needed for older versions
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}