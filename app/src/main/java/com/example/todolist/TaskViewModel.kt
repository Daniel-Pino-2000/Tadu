package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Task
import com.example.todolist.data.TaskRepository
import com.example.todolist.data.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = Graph.taskRepository
): ViewModel() {

    var taskHasBeenChanged by mutableStateOf(false)
    var taskTitleState by mutableStateOf("")
    var taskDescriptionState by mutableStateOf("")
    var taskDateState by mutableStateOf("")
    var taskAddressState by mutableStateOf("")
    var taskPriority: String by mutableStateOf("")
    var taskDeadline by mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentScreen: MutableState<Screen> = mutableStateOf(Screen.BottomScreen.Today)

    val currentScreen: MutableState<Screen> @RequiresApi(Build.VERSION_CODES.O)
    get() = _currentScreen

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun onTaskTitleChanged(newString:String){
        taskTitleState = newString
        taskHasBeenChanged = true
    }

    fun onTaskDescriptionChanged(newString: String){
        taskDescriptionState = newString
        taskHasBeenChanged = true
    }

    fun onTaskDateChanged(newString: String) {
        taskDateState = newString
        taskHasBeenChanged = true
    }

    fun onAddressChanged(newString: String) {
        taskAddressState = newString
        taskHasBeenChanged = true
    }

    fun onTaskPriorityChanged(newString: String) {
        taskPriority = newString
        taskHasBeenChanged = true
    }

    fun onTaskDeadlineChanged(newString: String) {
        taskDeadline = newString
        taskHasBeenChanged = true
    }

    // Initialize flows directly - they're already reactive
    val getAllTasks: Flow<List<Task>> = taskRepository.getTasks()
    val getPendingTasks: Flow<List<Task>> = taskRepository.getPendingTasks()
    val getCompletedTasks: Flow<List<Task>> = taskRepository.getCompletedTasks()
    val getDeletedTasks: Flow<List<Task>> = taskRepository.getDeletedTasks()

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

    // Hard delete - permanently removes from database
    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteATask(task)
        }
    }

    // Soft delete - marks as deleted but keeps in database
    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.softDeleteTask(taskId)
        }
    }

    // Mark task as completed
    fun completeTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.markTaskCompleted(taskId)
        }
    }

    // Mark task as pending (uncompleted)
    fun uncompleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.markTaskPending(taskId)
        }
    }

    // Restore deleted task
    fun restoreTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.restoreTask(taskId)
        }
    }

    // UI setter functions
    fun setShowBottomSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBottomSheet = show)
    }

    fun setTaskBeingEdited(editing: Boolean) {
        _uiState.value = _uiState.value.copy(taskBeingEdited = editing)
    }

    fun setShowDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    fun setTaskToUpdate(task: Task) {
        _uiState.value = _uiState.value.copy(taskToUpdate = task)
    }

    fun setId(id: Long) {
        _uiState.value = _uiState.value.copy(currentId = id)
    }
}