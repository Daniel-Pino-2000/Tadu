package com.example.todolist.data

import java.util.Date

data class Task(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val date: String = Date().toString(),
    val address: String = "",
    val priority: Int = 0
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