package com.myapp.tadu.data

import com.myapp.tadu.data.remote.TaskCloudService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class TaskRepository(
    private val taskDao: TaskDao,
    private val cloudService: TaskCloudService
) {
    // Add a new task
    suspend fun addTask(task: Task) {
        // Assign a stable ID if this is a new task (id = 0 means Room will auto-generate)
        val taskWithId = if (task.id == 0L) {
            task.copy(id = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE)
        } else {
            task
        }

        taskDao.addTask(taskWithId)
        cloudService.saveTask(taskWithId)
    }

    // Get tasks
    fun getTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    fun getDeletedTasks(): Flow<List<Task>> = taskDao.getDeletedTasks()
    fun getFinishedTasks(): Flow<List<Task>> = taskDao.getFinishedTasks()
    fun getTasksWithReminders(): Flow<List<Task>> = taskDao.getTasksWithReminders()
    fun getAllLabels(): Flow<List<String>> = taskDao.getAllLabels()
    fun getTasksByLabel(label: String): Flow<List<Task>> = taskDao.getTasksByLabel(label)
    fun getTaskById(id: Long): Flow<Task> = taskDao.getTaskById(id)

    // Soft delete a task
    suspend fun softDeleteTask(taskId: Long) {
        taskDao.softDeleteTask(taskId)
        val task = taskDao.getTaskById(taskId).first()
        cloudService.saveTask(task)
    }

    // Mark a task as completed
    suspend fun markTaskCompleted(taskId: Long) {
        taskDao.markTaskCompleted(taskId)
        val task = taskDao.getTaskById(taskId).first()
        cloudService.saveTask(task)
    }

    // Mark a task as pending (uncompleted)
    suspend fun markTaskPending(taskId: Long) {
        taskDao.markTaskPending(taskId)
        val task = taskDao.getTaskById(taskId).first()
        cloudService.saveTask(task)
    }

    // Restore a deleted task
    suspend fun restoreTask(taskId: Long) {
        taskDao.restoreTask(taskId)
        val task = taskDao.getTaskById(taskId).first()
        cloudService.saveTask(task)
    }

    // Update a task
    suspend fun updateATask(task: Task) {
        taskDao.updateATask(task)
        cloudService.saveTask(task)
    }

    // Delete a task permanently
    suspend fun deleteATask(task: Task) {
        taskDao.deleteATask(task)
        cloudService.deleteTask(task)
    }

    // Sync all tasks from cloud safely
    suspend fun syncFromCloud() {
        val cloudTasks = cloudService.getAllTasks()
        cloudTasks.forEach { task ->
            taskDao.upsertTask(task)
        }
    }

    // TaskRepository.kt
    suspend fun clearLocalTasks() {
        taskDao.clearAllTasks()
    }

}
