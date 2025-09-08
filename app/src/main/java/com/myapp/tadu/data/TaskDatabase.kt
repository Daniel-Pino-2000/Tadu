package com.myapp.tadu.data

import androidx.room.RoomDatabase
import androidx.room.Database

@Database(
    entities =[Task::class],
    version = 1,
    exportSchema = false

)

abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDao
}