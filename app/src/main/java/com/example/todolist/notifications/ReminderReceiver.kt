package com.example.todolist.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.todolist.settings.createSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ReminderReceiver", "Received reminder broadcast")

        val title = intent?.getStringExtra("TASK_TITLE") ?: return
        val taskId = intent?.getLongExtra("TASK_ID", -1) ?: -1
        val description = intent?.getStringExtra("TASK_DESCRIPTION") ?: ""
        val reminderText = intent?.getStringExtra("REMINDER_TEXT") ?: ""

        // Check system notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("ReminderReceiver", "System notification permission not granted")
                return
            }
        }

        // Use goAsync() for coroutine operations in BroadcastReceiver
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if notifications are enabled in app settings
                val settingsRepository = context.createSettingsRepository()
                val notificationsEnabled = settingsRepository.notificationsEnabled.first()

                if (!notificationsEnabled) {
                    Log.d("ReminderReceiver", "Notifications disabled in app settings")
                    pendingResult.finish()
                    return@launch
                }

                // Show notification if all checks pass
                showNotification(context, title, description, reminderText)
                Log.d("ReminderReceiver", "Notification shown for task: $title")

            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error processing notification", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}