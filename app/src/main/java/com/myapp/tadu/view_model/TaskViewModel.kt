package com.myapp.tadu.view_model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.tadu.Graph
import com.myapp.tadu.data.Task
import com.myapp.tadu.data.TaskRepository
import com.myapp.tadu.data.UiState
import com.myapp.tadu.navigation.Screen
import com.myapp.tadu.notifications.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = Graph.taskRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    var taskHasBeenChanged by mutableStateOf(false)
    var taskTitleState by mutableStateOf("")
    var taskDescriptionState by mutableStateOf("")
    var taskDateState by mutableStateOf("")
    var taskAddressState by mutableStateOf("")
    var taskPriority by mutableStateOf("")
    var taskDeadline by mutableStateOf("")
    var taskLabel by mutableStateOf("")
    var taskReminderTime: Long? by mutableStateOf(0L)
    var taskReminderText: String? by mutableStateOf("")

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.BottomScreen.Today)
    val currentScreen: StateFlow<Screen>
        @RequiresApi(Build.VERSION_CODES.O)
    get() = _currentScreen

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentRoute = MutableStateFlow<String>(Screen.BottomScreen.Today.route)
    val currentRoute: StateFlow<String>
        @RequiresApi(Build.VERSION_CODES.O)
    get() = _currentRoute

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCurrentScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCurrentRoute(route: String) {
        _currentRoute.value = route
    }

    fun onTaskTitleChanged(newString: String) {
        taskTitleState = newString
        taskHasBeenChanged = true
    }

    fun onTaskDescriptionChanged(newString: String) {
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

    fun onTaskLabelsChanged(newLabel: String) {
        taskLabel = newLabel
        taskHasBeenChanged = true
    }

    fun onTaskReminderTimeChanged(newReminder: Long?) {
        taskReminderTime = newReminder
        taskHasBeenChanged = true
    }

    fun onTaskReminderTextChanged(newReminderText: String?) {
        taskReminderText = newReminderText
        taskHasBeenChanged = true
    }

    fun scheduleAllReminders() {
        viewModelScope.launch {
            getTasksWithReminders.collect { tasks ->
                tasks
                    .filter { it.reminderTime != null && it.reminderTime!! > System.currentTimeMillis() }
                    .forEach { task ->
                        reminderScheduler.schedule(task)
                    }
            }
        }
    }





    fun resetFormFields() {
        taskTitleState = ""
        taskDescriptionState = ""
        taskDateState = ""
        taskAddressState = ""
        taskPriority = ""
        taskDeadline = ""
        taskLabel = ""
        taskReminderTime = null
        taskReminderText = null
        taskHasBeenChanged = false
    }

    fun resetUiState() {
        _uiState.value = UiState()
    }

    fun populateFieldsWithTask(task: Task) {
        taskTitleState = task.title
        taskDescriptionState = task.description
        taskAddressState = task.address
        taskDeadline = task.deadline
        taskPriority = task.priority
        taskLabel = task.label
        taskReminderTime = task.reminderTime
        taskReminderText = task.reminderText
        taskHasBeenChanged = false
    }

    val getAllTasks: Flow<List<Task>> = taskRepository.getTasks()
    val getPendingTasks: Flow<List<Task>> = taskRepository.getPendingTasks()
    val getTasksWithReminders: Flow<List<Task>> = taskRepository.getTasksWithReminders()
    val getCompletedTasks: Flow<List<Task>> = taskRepository.getCompletedTasks()
    val getDeletedTasks: Flow<List<Task>> = taskRepository.getDeletedTasks()
    val getFinishedTasks: Flow<List<Task>> = taskRepository.getFinishedTasks()
    val getAllLabels: Flow<List<String>> = taskRepository.getAllLabels()

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.addTask(task)
            // Schedule reminder if time is set and in future
            if (task.reminderTime != null && task.reminderTime!! > System.currentTimeMillis()) {
                println("It is scheduling the reminder!")
                reminderScheduler.schedule(task)
            }
        }
        resetFormFields()
        resetUiState()


    }


    fun getTaskById(id: Long): Flow<Task> {
        return taskRepository.getTaskById(id)
    }

    fun updateTask(updatedTask: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get the existing task (with old reminder info)
            val oldTask = taskRepository.getTaskById(updatedTask.id).firstOrNull()

            if (oldTask != null) {
                reminderScheduler.cancel(oldTask) // Cancel using original PendingIntent
            }

            taskRepository.updateATask(updatedTask)

            // Schedule new reminder
            if (updatedTask.reminderTime != null && updatedTask.reminderTime!! > System.currentTimeMillis()) {
                reminderScheduler.schedule(updatedTask)
            }
        }
        resetFormFields()
        resetUiState()
    }



    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            if (task.reminderTime != null) {
                reminderScheduler.cancel(task)
            }
            taskRepository.deleteATask(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = taskRepository.getTaskById(taskId).firstOrNull()
            if (task?.reminderTime != null) {
                reminderScheduler.cancel(task)
            }
            taskRepository.softDeleteTask(taskId)
        }
    }


    fun completeTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.markTaskCompleted(taskId)
        }
    }

    fun uncompleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.markTaskPending(taskId)
        }
    }

    fun restoreTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.restoreTask(taskId)
        }
        resetFormFields()
    }

    fun getTasksByLabel(label: String): Flow<List<Task>> {
        return taskRepository.getTasksByLabel(label)
    }



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