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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todolist.data.Task

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    viewModel: TaskViewModel = viewModel(),
    navController: NavHostController  // ✅ now passed from above
) {

    val uiState by viewModel.uiState.collectAsState()

    // Animation configuration
    val animationDuration = 350
    val slideDistance = 300

    NavHost(
        navController = navController,
        startDestination = Screen.BottomScreen.Today.bRoute
    ) {
        listOf(
            Screen.BottomScreen.Today,
            Screen.BottomScreen.Inbox,
            Screen.BottomScreen.Search
        ).forEach { screen ->
            composable(
                route = screen.bRoute,
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(animationDuration, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                HomeView(navController, viewModel)
            }
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

            // Animated bottom sheet
            if (uiState.showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        viewModel.setShowBottomSheet(false)
                        viewModel.setTaskBeingEdited(false)
                        selectedTaskId = null
                    }
                ) {
                    // Animate the content inside the bottom sheet
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