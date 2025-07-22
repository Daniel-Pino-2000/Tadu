package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todolist.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(viewModel: TaskViewModel = viewModel(),
               navController: NavHostController = rememberNavController()) {

    val uiState by viewModel.uiState.collectAsState()

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
            val uiState by viewModel.uiState.collectAsState()
            var selectedTaskId by remember { mutableStateOf<Long?>(null) }

            TaskCalendarView(
                viewModel,
                onTaskClick = { task ->
                    if (task != null) {
                        // Editing existing task
                        selectedTaskId = task.id
                        viewModel.setTaskBeingEdited(true)
                        viewModel.setShowBottomSheet(true)
                    } else {
                        // Adding new task
                        selectedTaskId = null
                        viewModel.setTaskBeingEdited(false)
                        viewModel.setShowBottomSheet(true)
                    }
                }
            )

            if (uiState.showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setShowBottomSheet(false)
                        viewModel.setTaskBeingEdited(false)
                        selectedTaskId = null
                    }
                ) {
                    AddTaskView(
                        selectedTaskId ?: uiState.currentId, // Use selected task ID if editing, otherwise use currentId
                        viewModel,
                        onDismiss = {
                            viewModel.setShowBottomSheet(false)
                            viewModel.setTaskBeingEdited(false)
                            selectedTaskId = null
                            viewModel.resetFormFields()
                        },
                        onSubmit = { task ->
                            if (!uiState.taskBeingEdited) {
                                viewModel.addTask(task)
                            } else {
                                viewModel.updateTask(task)
                            }
                            viewModel.setShowBottomSheet(false)
                            viewModel.setTaskBeingEdited(false)
                            selectedTaskId = null
                            viewModel.resetFormFields()
                        }
                    )
                }
            }
        }
    }
}