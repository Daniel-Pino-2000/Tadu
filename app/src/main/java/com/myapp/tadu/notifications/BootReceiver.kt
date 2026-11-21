package com.myapp.tadu.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.myapp.tadu.Graph
import com.myapp.tadu.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted — rescheduling reminders")

            val taskRepository = Graph.taskRepository
            val scheduler = AndroidReminderScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                // Reschedule reminders
                rescheduleReminders(taskRepository, scheduler)

            }
        }
    }

    private suspend fun rescheduleReminders(taskRepository: TaskRepository, scheduler: AndroidReminderScheduler) {
        try {
            val now = System.currentTimeMillis()
            val tasks = taskRepository.getTasksWithReminders().first()

            val validTasks = tasks.filter { it.reminderTime != null && it.reminderTime!! > now }
            validTasks.forEach { scheduler.schedule(it) }

            Log.d("BootReceiver", "Rescheduled ${validTasks.size} reminder(s)")
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error rescheduling reminders", e)
        }
    }
}