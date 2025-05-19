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

    // Loads all task from the task table
    @Query("Select * from `task-table`")
    abstract fun getAllTasks():Flow<List<Task>>

    @Update
    abstract suspend fun updateATask(taskEntity: Task)

    @Delete
    abstract suspend fun deleteATask(taskEntity: Task)

    @Query("Select * from `task-table` where id =:id")
    abstract fun getTaskById(id: Long): Flow<Task>
}