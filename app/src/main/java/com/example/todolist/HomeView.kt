package com.example.todolist

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.todolist.data.Task

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeView(navController: NavHostController, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val undoToastManager = remember { UndoToastManager(snackbarHostState, coroutineScope) }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by viewModel.currentRoute.collectAsState()

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            if (route != Screen.History.route && route != Screen.Calendar.route) {
                val screen = screenInBottom.find { it.bRoute == route }
                if (screen != null) {
                    viewModel.setCurrentScreen(screen)
                }
            }
        }
    }



    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentScreen is Screen.BottomScreen) {
                BottomBar(currentScreen, currentRoute, viewModel)
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,

        topBar = {
            if (currentRoute in setOf(
                    Screen.BottomScreen.Today.bRoute,
                    Screen.BottomScreen.Inbox.bRoute,
                    Screen.BottomScreen.Search.bRoute,
                    Screen.History.route,
                    Screen.Calendar.route
                )
            ) {
                (currentScreen as? Screen.BottomScreen)?.let { screen ->
                    AppBarView(title = screen.title, navController = navController)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                containerColor = colorResource(id = R.color.nice_blue),
                onClick = {
                    viewModel.setTaskBeingEdited(false)
                    viewModel.setShowBottomSheet(true)
                    viewModel.setId(0L)
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { innerPadding ->
        when (currentRoute) {
            Screen.BottomScreen.Today.bRoute,
            Screen.BottomScreen.Inbox.bRoute,
            Screen.BottomScreen.Search.bRoute-> {
                BottomNavScreens(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    undoToastManager = undoToastManager,
                    modifier = Modifier.padding(innerPadding)
                )
            }

        }
    }

    if (uiState.showDatePicker) {
        DatePicker { selectedDate ->
            viewModel.onTaskDeadlineChanged(selectedDate)
            val newTask = Task(
                id = uiState.taskToUpdate.id,
                title = uiState.taskToUpdate.title,
                description = uiState.taskToUpdate.description,
                date = uiState.taskToUpdate.date,
                address = uiState.taskToUpdate.address,
                priority = uiState.taskToUpdate.priority,
                deadline = selectedDate,
                label = uiState.taskToUpdate.label
            )
            viewModel.updateTask(newTask)
        }
        viewModel.setShowDatePicker(false)
    }

    if (uiState.showBottomSheet) {
        AddTaskView(
            uiState.currentId,
            viewModel,
            onDismiss = {
                viewModel.setShowBottomSheet(false)
                viewModel.setTaskBeingEdited(false)
                viewModel.resetFormFields()
                viewModel.resetUiState()
            },
            onSubmit = { task ->
                if (!uiState.taskBeingEdited) {
                    viewModel.addTask(task)
                } else {
                    viewModel.updateTask(task)
                }
                viewModel.setShowBottomSheet(false)
                viewModel.setTaskBeingEdited(false)
                viewModel.resetFormFields()
                viewModel.resetUiState()
            }
        )
    }
}