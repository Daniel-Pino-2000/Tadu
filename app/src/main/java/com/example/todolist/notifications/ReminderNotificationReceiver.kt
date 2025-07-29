package com.example.todolist.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.todolist.data.Task

class ReminderNotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val taskTitle = intent?.getStringExtra("TASK_TITLE") ?: return

        val service = ReminderNotificationService(context)
        service.showNotification(taskTitle)
    }

}


@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleReminder(context: Context, task: Task) {
    if (!task.reminderEnabled) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Check if exact alarms are allowed (API 31+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w("scheduleReminder", "Exact alarms not allowed; skipping scheduling for task ${task.id}")
            // Optionally, notify user or handle fallback here
            return
        }
    }

    val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
        putExtra("TASK_TITLE", task.title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.toInt(),
        intent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        else PendingIntent.FLAG_UPDATE_CURRENT
    )

    try {
        val triggerTime = task.reminderTriggerTime
        if (triggerTime == null) return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

    } catch (securityException: SecurityException) {
        Log.e("scheduleReminder", "Failed to schedule exact alarm for task ${task.id}", securityException)
        // Optionally handle fallback here
    }
}
