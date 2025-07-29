package com.example.todolist

import com.example.todolist.data.TaskDatabase
import com.example.todolist.data.TaskRepository
import android.content.Context
import androidx.room.Room

object Graph {
    lateinit var database: TaskDatabase
    lateinit var appContext: Context

    val taskRepository by lazy {
        TaskRepository(taskDao = database.taskDao())
    }

    fun provide(context: Context) {
        appContext = context.applicationContext  // <--- add this line
        database = Room.databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            "todolist.db"
        ).build()
    }
}

