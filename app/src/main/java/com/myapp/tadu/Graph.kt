package com.myapp.tadu

import android.content.Context
import androidx.room.Room
import com.myapp.tadu.data.TaskDatabase
import com.myapp.tadu.data.TaskRepository
import com.myapp.tadu.notifications.AndroidReminderScheduler
import com.myapp.tadu.notifications.ReminderScheduler

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