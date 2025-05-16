package com.example.todolist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class TaskViewModel(): ViewModel() {

    var taskTitleState by mutableStateOf("")
    var taskDescriptionState by mutableStateOf("")
    var taskDateState by mutableStateOf("")
    var taskAddressState by mutableStateOf("")

    fun onTaskTitleChanged(newString:String){
        taskTitleState = newString
    }

    fun onTaskDescriptionChanged(newString: String){
        taskDescriptionState = newString
    }

    fun onTaskDateChanged(newString: String) {
        taskDateState = newString
    }

    fun onAddressChanged(newString: String) {
        taskAddressState = newString
    }
}