package com.example.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// Listens to an event when the notification is triggered
class ReminderReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ReminderReceiver", "Received reminder broadcast")
        val title = intent?.getStringExtra("TASK_TITLE") ?: return
        showNotification(context, title)
    }

}