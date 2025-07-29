package com.example.todolist

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Task
import com.example.todolist.data.TaskRepository
import com.example.todolist.data.UiState
import com.example.todolist.notifications.scheduleReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = Graph.taskRepository
) : ViewModel() {

    var taskHasBeenChanged by mutableStateOf(false)
    var taskTitleState by mutableStateOf("")
    var taskDescriptionState by mutableStateOf("")
    var taskDateState by mutableStateOf("")
    var taskAddressState by mutableStateOf("")
    var taskPriority by mutableStateOf("")
    var taskDeadline by mutableStateOf("")
    var taskLabel by mutableStateOf("")
    var taskReminderEnabled by mutableStateOf(false)
    var taskReminderPreset by mutableStateOf("")
    var taskReminderPresetTime by mutableStateOf("")
    var taskReminderCustomDateTime by mutableStateOf<Long?>(null)
    var taskReminderTriggerTime by mutableStateOf<Long?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.BottomScreen.Today)
    val currentScreen: StateFlow<Screen> @RequiresApi(Build.VERSION_CODES.O)
    get() = _currentScreen

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentRoute = MutableStateFlow<String>(Screen.BottomScreen.Today.route)
    val currentRoute: StateFlow<String> @RequiresApi(Build.VERSION_CODES.O)
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

    fun onReminderEnabledChanged(enabled: Boolean) {
        taskReminderEnabled = enabled
        taskHasBeenChanged = true
    }

    fun onReminderPresetChanged(preset: String, time: String) {
        taskReminderPreset = preset
        taskReminderPresetTime = time
        taskHasBeenChanged = true
    }

    fun onReminderCustomDateTimeChanged(timestamp: Long) {
        taskReminderCustomDateTime = timestamp
        taskHasBeenChanged = true
    }

    fun onReminderTriggerTimeChanged(trigger: Long) {
        taskReminderTriggerTime = trigger
        taskHasBeenChanged = true
    }


    fun resetFormFields() {
        taskTitleState = ""
        taskDescriptionState = ""
        taskDateState = ""
        taskAddressState = ""
        taskPriority = ""
        taskDeadline = ""
        taskLabel = ""
        taskReminderEnabled = false
        taskReminderPreset = ""
        taskReminderPresetTime = ""
        taskReminderCustomDateTime = 0L
        taskReminderTriggerTime = 0L
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
        taskReminderEnabled = task.reminderEnabled
        taskReminderPreset = task.reminderType
        taskReminderPresetTime = task.reminderPresetTime
        taskReminderCustomDateTime = task.reminderCustomDateTime
        taskReminderTriggerTime = task.reminderTriggerTime
        taskHasBeenChanged = false
    }


    val getAllTasks: Flow<List<Task>> = taskRepository.getTasks()
    val getPendingTasks: Flow<List<Task>> = taskRepository.getPendingTasks()
    val getTasksWithReminders: Flow<List<Task>> = taskRepository.getTasksWithReminders()
    val getCompletedTasks: Flow<List<Task>> = taskRepository.getCompletedTasks()
    val getDeletedTasks: Flow<List<Task>> = taskRepository.getDeletedTasks()
    val getFinishedTasks: Flow<List<Task>> = taskRepository.getFinishedTasks()
    val getAllLabels: Flow<List<String>> = taskRepository.getAllLabels()

    @SuppressLint("ScheduleExactAlarm")
    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.addTask(task)

            if (task.reminderEnabled) {
                tryScheduleReminder(task)
            }
        }
        resetFormFields()
        resetUiState()
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateATask(task)

            if (task.reminderEnabled) {
                tryScheduleReminder(task)
            }
        }
        resetFormFields()
        resetUiState()
    }


    fun getTaskById(id: Long): Flow<Task> {
        return taskRepository.getTaskById(id)
    }

    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteATask(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
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

    private fun tryScheduleReminder(task: Task) {
        val context = Graph.appContext
        val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)

        val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }

        if (canSchedule) {
            try {
                scheduleReminder(context, task)
            } catch (e: SecurityException) {
                // Log or inform the user
                e.printStackTrace()
            }
        } else {
            // Could log or notify that exact alarm can't be scheduled
        }
    }

}
