package com.example.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderNotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val taskId = intent?.getIntExtra("TASK_ID", -1) ?: return
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Unnamed Task"

        val service = ReminderNotificationService(context)
        //service.showNotification(taskId, taskTitle)
    }
}