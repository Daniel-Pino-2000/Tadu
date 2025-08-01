package com.example.todolist.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    suspend fun addTask(task: Task) {
        taskDao.addTask(task)
    }

    fun getTasks() : Flow<List<Task>> =taskDao.getAllTasks()

    fun getPendingTasks() : Flow<List<Task>> = taskDao.getPendingTasks()

    fun getCompletedTasks() : Flow<List<Task>> = taskDao.getCompletedTasks()

    fun getDeletedTasks() : Flow<List<Task>> = taskDao.getDeletedTasks()

    fun getFinishedTasks() : Flow<List<Task>> = taskDao.getFinishedTasks()

    // Get all labels for dropdown/filter
    fun getAllLabels(): Flow<List<String>> = taskDao.getAllLabels()

    // Get tasks by specific label
    fun getTasksByLabel(label: String): Flow<List<Task>> = taskDao.getTasksByLabel(label)


    // In TaskRepository
    suspend fun softDeleteTask(taskId: Long) {
        taskDao.softDeleteTask(taskId)
    }

    suspend fun markTaskCompleted(taskId: Long) {
        taskDao.markTaskCompleted(taskId)
    }

    suspend fun markTaskPending(taskId: Long) {
        taskDao.markTaskPending(taskId)
    }

    suspend fun restoreTask(taskId: Long) {
        taskDao.restoreTask(taskId)
    }


    fun getTaskById(id: Long):Flow<Task> {
        return taskDao.getTaskById(id)
    }

    suspend fun updateATask(task: Task) {
        taskDao.updateATask(task)
    }

    suspend fun deleteATask(task: Task) {
        taskDao.deleteATask(task)
    }



}