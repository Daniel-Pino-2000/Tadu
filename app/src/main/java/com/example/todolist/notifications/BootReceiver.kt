package com.example.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todolist.Graph // assuming your DI or repository access is here
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
                val now = System.currentTimeMillis()
                val tasks = taskRepository.getTasksWithReminders().first() // get current list

                tasks.filter { it.reminderTime != null && it.reminderTime!! > now }
                    .forEach { scheduler.schedule(it) }
            }
        }
    }
}

