package com.example.todolist

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UndoToastManager(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {
    private var currentUndoJob: Job? = null

    /**
     * Shows a toast with undo option for task actions with immediate state change
     * @param message The message to display (e.g., "Task completed", "Task deleted")
     * @param onImmediateChange The action to execute immediately (e.g., complete the task)
     * @param onUndo The undo action to execute if user taps undo (e.g., restore function)
     */
    fun showUndoToast(
        message: String,
        onImmediateChange: suspend () -> Unit,
        onUndo: suspend () -> Unit
    ) {
        // Cancel any existing undo job
        currentUndoJob?.cancel()

        currentUndoJob = coroutineScope.launch {
            // Execute immediate change (e.g., complete the task)
            onImmediateChange()

            val snackbarResult = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )

            when (snackbarResult) {
                SnackbarResult.ActionPerformed -> {
                    // User clicked undo - restore the task
                    onUndo()
                }
                SnackbarResult.Dismissed -> {
                    // Toast dismissed naturally or by user swipe
                    // No additional action needed - task is already completed
                }
            }
        }
    }

    /**
     * Convenience function specifically for task completion with immediate state change
     */
    fun showTaskCompletedToast(
        taskName: String = "Task",
        onComplete: suspend () -> Unit,
        onRestore: suspend () -> Unit
    ) {
        showUndoToast(
            message = "$taskName completed",
            onImmediateChange = onComplete,
            onUndo = onRestore
        )
    }

    /**
     * Convenience function specifically for task deletion with immediate state change
     */
    fun showTaskDeletedToast(
        taskName: String = "Task",
        onDelete: suspend () -> Unit,
        onRestore: suspend () -> Unit
    ) {
        showUndoToast(
            message = "$taskName deleted",
            onImmediateChange = onDelete,
            onUndo = onRestore
        )
    }

    /**
     * Cancel any pending undo action
     */
    fun cancelPendingUndo() {
        currentUndoJob?.cancel()
    }
}

// Extension function to make it easier to use in your existing code
suspend fun UndoToastManager.showTaskActionToast(
    action: TaskAction,
    taskName: String = "Task",
    onExecute: suspend () -> Unit,
    onRestore: suspend () -> Unit
) {
    when (action) {
        TaskAction.COMPLETE -> showTaskCompletedToast(taskName, onExecute, onRestore)
        TaskAction.DELETE -> showTaskDeletedToast(taskName, onExecute, onRestore)
    }
}

enum class TaskAction {
    COMPLETE,
    DELETE
}