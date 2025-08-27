package com.example.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todolist.Graph
import com.example.todolist.data.TaskRepository
import com.example.todolist.settings.HistoryCleanupWorker
import com.example.todolist.settings.SettingsRepository
import com.example.todolist.settings.createSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted — rescheduling reminders and history cleanup")

            val taskRepository = Graph.taskRepository
            val settingsRepository = context.createSettingsRepository()
            val scheduler = AndroidReminderScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                // Reschedule reminders
                rescheduleReminders(taskRepository, scheduler)

                // Reschedule history cleanup if enabled
                rescheduleHistoryCleanupIfEnabled(context, settingsRepository)
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

    private suspend fun rescheduleHistoryCleanupIfEnabled(
        context: Context,
        settingsRepository: SettingsRepository
    ) {
        try {
            val isClearHistoryEnabled = settingsRepository.clearHistoryEnabled.first()

            if (isClearHistoryEnabled) {
                Log.d("BootReceiver", "History cleanup is enabled, rescheduling work")
                scheduleHistoryCleanup(context)
            } else {
                Log.d("BootReceiver", "History cleanup is disabled")
            }
        } catch (e: Exception) {
            Log.e("BootReceiver", "Error checking history cleanup setting", e)
        }
    }

    private fun scheduleHistoryCleanup(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Cancel any existing work first to ensure clean state
        workManager.cancelUniqueWork("history_cleanup_work")

        val cleanupWorkRequest = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(30, TimeUnit.DAYS)
            .setInitialDelay(30, TimeUnit.DAYS) // Ensure it waits 30 days before first execution
            .build()

        workManager.enqueueUniquePeriodicWork(
            "history_cleanup_work",
            ExistingPeriodicWorkPolicy.KEEP, // Use KEEP to prevent immediate execution
            cleanupWorkRequest
        )

        Log.d("BootReceiver", "History cleanup work scheduled with 30-day initial delay")
    }
}