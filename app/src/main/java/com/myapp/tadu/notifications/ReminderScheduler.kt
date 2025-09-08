package com.myapp.tadu.notifications

import com.myapp.tadu.data.Task

interface ReminderScheduler {
    fun schedule(task: Task)
    fun cancel(task: Task)
}