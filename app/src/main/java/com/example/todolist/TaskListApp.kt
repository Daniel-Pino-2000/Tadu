package com.example.todolist

import android.app.Application

class TaskListApp:Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}