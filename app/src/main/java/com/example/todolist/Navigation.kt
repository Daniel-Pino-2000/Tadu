package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.ui.settings.SettingsScreen
import com.example.todolist.notifications.AndroidReminderScheduler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController  // now passed from above
) {
    val context = LocalContext.current

    // Manually create ViewModel with injected ReminderScheduler and Repository
    val viewModel = remember {
        TaskViewModel(
            taskRepository = Graph.taskRepository, // or your actual repository instance
            reminderScheduler = AndroidReminderScheduler(context)
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // Animation configuration
    val animationDuration = 350

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home screen
        composable(Screen.Home.route) {
            HomeView(navController, viewModel)
        }

        // History screen with vertical slide from bottom
        composable(
            Screen.History.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            TaskHistoryView(viewModel, navController)
        }

        // Reminders screen
        composable(
            Screen.Reminders.route
        ) {
            TaskRemindersScreen(viewModel, navController)
        }

        composable(
            Screen.Settings.route
        ) {
            SettingsScreen()
        }


        // Calendar screen with scale and fade animation
        composable(
            Screen.Calendar.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
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

            // Animated bottom sheet
            if (uiState.showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setShowBottomSheet(false)
                        viewModel.setTaskBeingEdited(false)
                        selectedTaskId = null
                    }
                ) {
                    AnimatedVisibility(
                        visible = uiState.showBottomSheet,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300, easing = EaseOutQuart)
                        ) + fadeIn(
                            animationSpec = tween(300, easing = EaseOutQuart)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(200, easing = EaseInQuart)
                        ) + fadeOut(
                            animationSpec = tween(200, easing = EaseInQuart)
                        )
                    ) {
                        AddTaskView(
                            selectedTaskId ?: uiState.currentId,
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
}
