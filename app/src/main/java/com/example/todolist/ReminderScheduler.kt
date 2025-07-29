package com.example.todolist

import android.annotation.SuppressLint
import android.content.Context
import com.example.todolist.notifications.scheduleReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ReminderScheduler {
    private val taskRepository = Graph.taskRepository

    @SuppressLint("ScheduleExactAlarm")
    fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = taskRepository.getTasksWithReminders().first()
            tasks.forEach { task ->
                if (task.reminderEnabled) {
                    scheduleReminder(context, task)
                }
            }
        }
    }
}
