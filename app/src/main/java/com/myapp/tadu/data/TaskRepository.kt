package com.myapp.tadu.data

import com.myapp.tadu.data.remote.TaskCloudService
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val cloudService: TaskCloudService
) {
    suspend fun addTask(task: Task) {
        taskDao.addTask(task)
        cloudService.saveTask(task)
    }

    fun getTasks() : Flow<List<Task>> =taskDao.getAllTasks()

    fun getPendingTasks() : Flow<List<Task>> = taskDao.getPendingTasks()

    fun getCompletedTasks() : Flow<List<Task>> = taskDao.getCompletedTasks()

    fun getDeletedTasks() : Flow<List<Task>> = taskDao.getDeletedTasks()

    fun getFinishedTasks() : Flow<List<Task>> = taskDao.getFinishedTasks()

    fun getTasksWithReminders() : Flow<List<Task>> = taskDao.getTasksWithReminders()

    // Get all labels for dropdown/filter
    fun getAllLabels(): Flow<List<String>> = taskDao.getAllLabels()

    // Get tasks by specific label
    fun getTasksByLabel(label: String): Flow<List<Task>> = taskDao.getTasksByLabel(label)


    // In TaskRepository
    suspend fun softDeleteTask(taskId: Long) {
        taskDao.softDeleteTask(taskId)
        taskDao.getTaskById(taskId).collect { task ->
            cloudService.deleteTask(task)
        }
    }

    suspend fun markTaskCompleted(taskId: Long) {
        taskDao.markTaskCompleted(taskId)
        taskDao.getTaskById(taskId).collect { task ->
            cloudService.saveTask(task)
        }
    }

    suspend fun markTaskPending(taskId: Long) {
        taskDao.markTaskPending(taskId)
        taskDao.getTaskById(taskId).collect { task ->
            cloudService.saveTask(task)
        }
    }

    suspend fun restoreTask(taskId: Long) {
        taskDao.restoreTask(taskId)
        taskDao.getTaskById(taskId).collect { task ->
            cloudService.saveTask(task)
        }
    }


    fun getTaskById(id: Long):Flow<Task> {
        return taskDao.getTaskById(id)
    }

    suspend fun updateATask(task: Task) {
        taskDao.updateATask(task)
        cloudService.saveTask(task)
    }

    suspend fun deleteATask(task: Task) {
        taskDao.deleteATask(task)
        cloudService.deleteTask(task)
    }

    // Optional: restore from cloud on app start
    suspend fun syncFromCloud() {
        val cloudTasks = cloudService.getAllTasks()
        cloudTasks.forEach { taskDao.addTask(it) }
    }



}