package com.myapp.tadu.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myapp.tadu.notifications.ReminderScheduler

class AuthViewModelFactory(
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                reminderScheduler = reminderScheduler
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
