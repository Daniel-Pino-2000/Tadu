package com.example.todolist.notifications

import com.example.todolist.data.Task

interface ReminderScheduler {
    fun schedule(task: Task)
    fun cancel(task: Task)
}