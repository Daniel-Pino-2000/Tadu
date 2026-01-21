package com.myapp.tadu.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.myapp.tadu.data.Task
import kotlinx.coroutines.tasks.await

class TaskCloudService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userTasksCollection() =
        auth.currentUser?.let { db.collection("users").document(it.uid).collection("tasks") }

    // Save or update a task
    suspend fun saveTask(task: Task) {
        val col = userTasksCollection() ?: return
        col.document(task.id.toString()).set(task, SetOptions.merge()).await()
    }

    // Delete a task in Firestore
    suspend fun deleteTask(task: Task) {
        val col = userTasksCollection() ?: return
        col.document(task.id.toString()).delete().await()
    }

    // Get all tasks for current user
    suspend fun getAllTasks(): List<Task> {
        val col = userTasksCollection() ?: return emptyList()
        val snapshot = col.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
    }
}
