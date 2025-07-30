package com.example.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Listens to an event when the notification is triggered
class ReminderReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("Task Title") ?: return
        println("Alarm triggered: $message")
    }
}