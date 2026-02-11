package com.myapp.tadu.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.tadu.data.TaskRepository
import com.myapp.tadu.notifications.AndroidReminderScheduler
import com.myapp.tadu.view_model.TaskViewModel

class TaskViewModelFactory(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: AndroidReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskRepository, reminderScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
