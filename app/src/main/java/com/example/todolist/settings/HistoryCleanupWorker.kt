package com.example.todolist.settings

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todolist.Graph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HistoryCleanupWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            Log.d("HistoryCleanupWorker", "Starting history cleanup - deleting ALL finished tasks...")

            val taskRepository = Graph.taskRepository

            runBlocking {
                // Get all finished tasks
                val finishedTasks = taskRepository.getFinishedTasks().first()

                Log.d("HistoryCleanupWorker", "Found ${finishedTasks.size} finished tasks to delete")

                if (finishedTasks.isNotEmpty()) {
                    // Delete ALL finished tasks
                    finishedTasks.forEach { task ->
                        taskRepository.deleteATask(task)
                        Log.d("HistoryCleanupWorker", "Deleted finished task: ${task.title}")
                    }

                    Log.d("HistoryCleanupWorker", "Successfully deleted ${finishedTasks.size} finished tasks")
                } else {
                    Log.d("HistoryCleanupWorker", "No finished tasks to delete")
                }

                Log.d("HistoryCleanupWorker", "History cleanup completed successfully")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("HistoryCleanupWorker", "Error during history cleanup", e)
            Result.retry()
        }
    }
}