package com.myapp.tadu.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "task-table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "task-title")
    var title: String = "",
    @ColumnInfo(name = "task-description")
    var description: String = "",
    @ColumnInfo(name = "task-date")
    var date: String = Date().toString(),
    @ColumnInfo(name = "task-address")
    var address: String = "",
    @ColumnInfo(name = "task-priority")
    var priority: String = "",  // ADDED DEFAULT VALUE
    @ColumnInfo(name = "task-deadline")
    var deadline: String = Date().toString(),

    @ColumnInfo(name = "task-deleted")
    var isDeleted: Boolean = false,
    @ColumnInfo(name = "task-deletion-date")
    var deletionDate: Long? = null,
    @ColumnInfo(name = "task-completed")
    var isCompleted: Boolean = false,
    @ColumnInfo(name = "task-completion-date")
    var completionDate: Long? = null,

    @ColumnInfo(name = "task-label")
    var label: String = "",  // Changed to var

    @ColumnInfo(name = "task-reminder-time")
    var reminderTime: Long? = null,

    @ColumnInfo(name = "task-reminder-text")
    var reminderText: String? = null
)