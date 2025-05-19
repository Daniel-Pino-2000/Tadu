package com.example.todolist

import com.example.todolist.data.TaskDatabase
import com.example.todolist.data.TaskRepository
import android.content.Context
import androidx.room.Room

object Graph {
    lateinit var database: TaskDatabase

    val taskRepository by lazy {
        TaskRepository(taskDao = database.taskDao())
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(context = context, TaskDatabase::class.java, "todolist.db").build()
    }
}