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
    val label: String = "",// Default label


    // Reminder data
    @ColumnInfo(name = "reminder-enabled")
    val reminderEnabled: Boolean = false,

    @ColumnInfo(name = "reminder-type")
    val reminderType: String = "PRESET",  // or enum stored as string/int

    @ColumnInfo(name = "reminder-preset-time")
    val reminderPresetTime: String = "15 minutes before",

    @ColumnInfo(name = "reminder-custom-datetime")
    val reminderCustomDateTime: Long? = null,  // store as epoch millis for LocalDateTime

    @ColumnInfo(name = "reminder-trigger-time")
    val reminderTriggerTime: Long? = null  // absolute time when alarm should trigger

)