package com.myapp.tadu.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myapp.tadu.AddTaskView
import com.myapp.tadu.Graph
import com.myapp.tadu.HomeView
import com.myapp.tadu.SettingsScreen
import com.myapp.tadu.TaskCalendarView
import com.myapp.tadu.TaskHistoryView
import com.myapp.tadu.TaskRemindersScreen
import com.myapp.tadu.view_model.TaskViewModel
import com.myapp.tadu.settings.createSettingsRepository
import com.myapp.tadu.settings.SettingsViewModel
import com.myapp.tadu.notifications.AndroidReminderScheduler
import com.myapp.tadu.screens.LoginScreen
import com.myapp.tadu.screens.SignUpScreen
import com.myapp.tadu.view_model.AuthViewModel
import com.myapp.tadu.view_model.AuthViewModelFactory
import com.myapp.tadu.view_model.TaskViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    isLoggedIn: Boolean
) {
    val context = LocalContext.current

    val reminderScheduler = AndroidReminderScheduler(context)

    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            taskRepository = Graph.taskRepository,
            reminderScheduler = reminderScheduler
        )
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            reminderScheduler = reminderScheduler
        )
    )

    val settingsViewModel = remember {
        val repo = context.createSettingsRepository()
        SettingsViewModel(repo)
    }

    val uiState by taskViewModel.uiState.collectAsState()

    val animationDuration = 350

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.LoginScreen.route
    ) {
        // Login screen
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignupScreen.route) },
                authViewModel = authViewModel,
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // Signup screen
        composable(Screen.SignupScreen.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.SignupScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // Home screen
        composable(Screen.Home.route) {
            HomeView(navController, taskViewModel, settingsViewModel)
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
            TaskHistoryView(taskViewModel, settingsViewModel, navController)
        }

        // Reminders screen
        composable(Screen.Reminders.route) {
            TaskRemindersScreen(taskViewModel, navController)
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(navController, settingsViewModel, authViewModel)
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
                taskViewModel,
                onTaskClick = { task ->
                    if (task != null) {
                        selectedTaskId = task.id
                        taskViewModel.setTaskBeingEdited(true)
                        taskViewModel.setShowBottomSheet(true)
                    } else {
                        selectedTaskId = null
                        taskViewModel.setTaskBeingEdited(false)
                        taskViewModel.setShowBottomSheet(true)
                    }
                }
            )

            // AddTaskView owns its own ModalBottomSheet internally — no wrapper needed here
            if (uiState.showBottomSheet) {
                AddTaskView(
                    id = selectedTaskId ?: uiState.currentId,
                    viewModel = taskViewModel,
                    settingsViewModel = settingsViewModel,
                    onDismiss = {
                        taskViewModel.setShowBottomSheet(false)
                        taskViewModel.setTaskBeingEdited(false)
                        selectedTaskId = null
                        taskViewModel.resetFormFields()
                    },
                    onSubmit = { task ->
                        if (!uiState.taskBeingEdited) {
                            taskViewModel.addTask(task)
                        } else {
                            taskViewModel.updateTask(task)
                        }
                        taskViewModel.setShowBottomSheet(false)
                        taskViewModel.setTaskBeingEdited(false)
                        selectedTaskId = null
                        taskViewModel.resetFormFields()
                    }
                )
            }
        }
    }
}