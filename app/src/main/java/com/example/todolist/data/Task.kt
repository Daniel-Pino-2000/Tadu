package com.example.todolist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "task-table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "task-title")
    val title: String = "",
    @ColumnInfo(name = "task-description")
    val description: String = "",
    @ColumnInfo(name = "task-date")
    val date: String = Date().toString(),
    @ColumnInfo(name = "task-address")
    val address: String = "",
    @ColumnInfo(name = "task-priority")
    val priority: String,
    @ColumnInfo(name = "task-deadline")
    var deadline: String = Date().toString(),

    @ColumnInfo(name = "task-deleted")
    var isDeleted: Boolean =  false,
    @ColumnInfo(name = "task-deletion-date")
    var deletionDate: Long? = null,
    @ColumnInfo(name = "task-completed")
    var isCompleted: Boolean =  false,
    @ColumnInfo(name = "task-completion-date")
    var completionDate: Long? = null,

    @ColumnInfo(name = "task-label")
    val label: String = ""// Default label

)