package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todolist.data.Task

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(viewModel: TaskViewModel = viewModel(),
               navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.BottomScreen.Today.bRoute
    ) {
        composable(Screen.BottomScreen.Today.bRoute) {
            HomeView(navController, viewModel)
        }
        composable(Screen.BottomScreen.Inbox.bRoute) {
            HomeView(navController, viewModel)
        }
        composable(Screen.BottomScreen.Search.bRoute) {
            HomeView(navController, viewModel)
        }

        composable(Screen.History.route) {
            TaskHistoryView(viewModel, navController)
        }

        composable(Screen.Calendar.route) {
            TaskCalendarView(
                viewModel,
                onTaskClick = { task ->
                    // Handle task click - you implement this
                    // e.g., navigate to task detail, show popup, etc.
                })
        }
    }
}