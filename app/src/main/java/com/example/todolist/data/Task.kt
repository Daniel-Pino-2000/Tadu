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
    val priority: String = "5",
    @ColumnInfo(name = "task-deadline")
    val deadline: String = Date().toString()
)

object DummyTask{
    val taskList = listOf(
        Task(title="Study",
            description =  "I have a quiz soon"),
        Task(title = "Exercise",
            description = "Go to the gym"),
        Task(title = "Devotional",
            description= "Prepare the class for the small group"),
        Task(title = "Call Eli",
            description = "Spend time with my girlfriend")
    )
}