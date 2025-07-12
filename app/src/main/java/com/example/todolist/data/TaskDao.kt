package com.example.todolist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addTask(taskEntity: Task)

    // Get active tasks only
    @Query("SELECT * FROM `task-table` WHERE `task-deleted` = 0")
    abstract fun getActiveTasks(): Flow<List<Task>>

    // Loads all task from the task table
    @Query("Select * from `task-table`")
    abstract fun getAllTasks():Flow<List<Task>>

    @Update
    abstract suspend fun updateATask(taskEntity: Task)

    // Hard delete (permanently remove from database)
    @Delete
    abstract suspend fun deleteATask(taskEntity: Task)

    // Soft delete (mark as deleted)
    @Query("UPDATE `task-table` SET `task-deleted` = 1, `task-deletion-date` = :timestamp WHERE id = :id")
    abstract suspend fun softDeleteTask(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("Select * from `task-table` where id =:id")
    abstract fun getTaskById(id: Long): Flow<Task>

    @Query("Select * from `task-table` where `task-deleted` = 0 AND `task-completed` = 0")
    abstract fun  getPendingTasks():Flow<List<Task>>

    @Query("Select * from `task-table` where `task-deleted` = 1 AND `task-completed` = 0")
    abstract fun  getDeletedTasks():Flow<List<Task>>

    @Query("Select * from `task-table` where `task-deleted` = 0 AND `task-completed` = 1")
    abstract fun  getCompletedTasks():Flow<List<Task>>

    @Query("Select * from `task-table` where `task-deleted` = 1 OR `task-completed` = 1")
    abstract fun  getFinishedTasks():Flow<List<Task>>

    // Mark task as completed
    @Query("UPDATE `task-table` SET `task-completed` = 1, `task-completion-date` = :timestamp WHERE id = :id")
    abstract suspend fun markTaskCompleted(id: Long, timestamp: Long = System.currentTimeMillis())

    // Mark task as pending (uncompleted)
    @Query("UPDATE `task-table` SET `task-completed` = 0, `task-completion-date` = NULL WHERE id = :id")
    abstract suspend fun markTaskPending(id: Long)

    // Restore deleted task
    @Query("UPDATE `task-table` SET `task-deleted` = 0, `task-deletion-date` = NULL, `task-completed` = 0, `task-completion-date` = NULL WHERE id = :id")
    abstract suspend fun restoreTask(id: Long)

    // Permanently delete old completed/deleted tasks
    @Query("DELETE FROM `task-table` WHERE (`task-deleted` = 1 OR `task-completed` = 1) AND (`task-deletion-date` < :cutoffTime OR `task-completion-date` < :cutoffTime)")
    abstract suspend fun permanentlyDeleteOldTasks(cutoffTime: Long)

    // Get task count by status
    @Query("SELECT COUNT(*) FROM `task-table` WHERE `task-deleted` = 0 AND `task-completed` = 0")
    abstract fun getPendingTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM `task-table` WHERE `task-completed` = 1 AND `task-deleted` = 0")
    abstract fun getCompletedTaskCount(): Flow<Int>

    // 1. Get all unique labels (for dropdown/filter options)
    @Query("SELECT DISTINCT `task-label` FROM `task-table` WHERE `task-deleted` = 0 ORDER BY `task-label`")
    abstract fun getAllLabels(): Flow<List<String>>

    // 2. Get tasks by specific label
    @Query("SELECT * FROM `task-table` WHERE `task-label` = :label AND `task-deleted` = 0")
    abstract fun getTasksByLabel(label: String): Flow<List<Task>>
}