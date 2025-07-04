package com.example.todolist

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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


    val currentScreen = remember {
        viewModel.currentScreen.value
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    Scaffold(
        bottomBar = {
            if (currentRoute != null) {
                BottomBar(currentScreen, currentRoute, navController)
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = { AppBarView(title = getScreenTitle(currentRoute), navController) },
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
                }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }

    ) { innerPadding ->
        // Use the existing navigation logic and show content based on current route
        when (currentRoute) {
            Screen.BottomScreen.Today.bRoute -> {
                BottomNavScreens(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.BottomScreen.Inbox.bRoute -> {
                BottomNavScreens(
                    viewModel = viewModel,
                    currentRoute = currentRoute,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                BottomNavScreens(
                    viewModel = viewModel,
                    currentRoute = "search",
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
                deadline = selectedDate
            )
            viewModel.updateTask(newTask) // When the user picks a date, update the task:
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
            },
            onSubmit = { task ->
                if (!uiState.taskBeingEdited) {
                    viewModel.addTask(task)  // ← Add to DB
                } else {
                    viewModel.updateTask(task)  // ← Update in DB
                }
                viewModel.setShowBottomSheet(false)
                viewModel.setTaskBeingEdited(false)
            }
        )
    }
}

// Option 2: More elegant approach using your screenInBottom list
@RequiresApi(Build.VERSION_CODES.O)
fun getScreenTitle(route: String?): String {
    return screenInBottom.find { it.bRoute == route }?.bTitle ?: "Today"
}

