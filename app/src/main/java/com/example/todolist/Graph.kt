package com.example.todolist

import android.content.Context
import androidx.room.Room
import com.example.todolist.data.TaskDatabase
import com.example.todolist.data.TaskRepository
import com.example.todolist.notifications.AndroidReminderScheduler
import com.example.todolist.notifications.ReminderScheduler

object Graph {
    lateinit var database: TaskDatabase
        private set

    lateinit var reminderScheduler: ReminderScheduler
        private set

    val taskRepository by lazy {
        TaskRepository(taskDao = database.taskDao())
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(
            context = context,
            TaskDatabase::class.java,
            "todolist.db"
        ).build()

        reminderScheduler = AndroidReminderScheduler(context)
    }
}