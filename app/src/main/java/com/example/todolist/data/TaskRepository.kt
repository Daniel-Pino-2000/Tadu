package com.example.todolist.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    suspend fun addTask(task: Task) {
        taskDao.addTask(task)
    }

    fun getTasks() : Flow<List<Task>> =taskDao.getAllTasks()

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