package com.example.todolist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Task
import com.example.todolist.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = Graph.taskRepository
): ViewModel() {

    var taskTitleState by mutableStateOf("")
    var taskDescriptionState by mutableStateOf("")
    var taskDateState by mutableStateOf("")
    var taskAddressState by mutableStateOf("")
    var taskPriority: String by mutableStateOf("")
    var taskDeadline by mutableStateOf("")


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

    fun onTaskPriorityChanged(newString: String) {
        taskPriority = newString
    }

    fun onTaskDeadlineChanged(newString: String) {
        taskDeadline = newString
    }

    lateinit var getAllTasks: Flow<List<Task>>

    init {
        viewModelScope.launch {
            getAllTasks = taskRepository.getTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.addTask(task)
        }
    }

    fun getTaskById(id: Long): Flow<Task> {
        return taskRepository.getTaskById(id)
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateATask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteATask(task)
            getAllTasks = taskRepository.getTasks()
        }
    }
}