package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.data.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavScreens(
    viewModel: TaskViewModel,
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
    val currentYear = today.year

    val taskList = viewModel.getPendingTasks.collectAsState(initial = listOf())
    val tasks = taskList.value

    // Observe UI state for keyboard management
    val uiState by viewModel.uiState.collectAsState()

    // Hide keyboard when bottom sheet is closed or task editing is completed
    LaunchedEffect(uiState.showBottomSheet, uiState.taskBeingEdited) {
        if (!uiState.showBottomSheet && !uiState.taskBeingEdited) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    val tasksToDisplay = when (currentRoute) {
        "today" -> tasks.filter { task ->
            if (task.deadline.isBlank()) {
                true // include tasks with blank deadline in "today"
            } else {
                try {
                    val taskDate = LocalDate.parse("${task.deadline} $currentYear", formatter)
                    !taskDate.isAfter(today) // deadline <= today
                } catch (e: Exception) {
                    false // exclude if parsing fails
                }
            }
        }
        "search" -> {
            if (searchQuery.isBlank()) {
                emptyList() // show no tasks when search is empty
            } else {
                tasks.filter { task ->
                    task.title.contains(searchQuery, ignoreCase = true)
                }
            }
        }
        "inbox" -> tasks
        else -> tasks
    }

    // Group tasks by deadline status and sort by priority
    data class TaskGroup(
        val title: String,
        val tasks: List<Task>,
        val color: Color
    )

    // Get colors outside of remember block
    val niceBlueColor = colorResource(id = R.color.nice_blue)

    val groupedTasks = remember(tasksToDisplay, today, currentRoute) {
        // For search route, don't group by deadline - just show all matching tasks
        if (currentRoute == "search") {
            val sortedTasks = tasksToDisplay.sortedBy { task ->
                (if (task.priority.isBlank()) "4" else task.priority).toInt()
            }
            if (sortedTasks.isNotEmpty()) {
                listOf(TaskGroup(
                    "Search Results",
                    sortedTasks,
                    niceBlueColor
                ))
            } else {
                emptyList()
            }
        } else {
            val overdueTasks = mutableListOf<Task>()
            val todayTasks = mutableListOf<Task>()
            val futureTasks = mutableListOf<Task>()

            tasksToDisplay.forEach { task ->
                if (task.deadline.isBlank()) {
                    todayTasks.add(task) // Tasks without deadline go to today
                } else {
                    try {
                        val taskDate = LocalDate.parse("${task.deadline} $currentYear", formatter)
                        when {
                            taskDate.isBefore(today) -> overdueTasks.add(task)
                            taskDate.isEqual(today) -> todayTasks.add(task)
                            else -> futureTasks.add(task)
                        }
                    } catch (e: Exception) {
                        todayTasks.add(task) // Default to today if parsing fails
                    }
                }
            }

            // Sort each group by priority (lower number = higher priority)
            val sortByPriority: (List<Task>) -> List<Task> = { taskList ->
                taskList.sortedBy { task ->
                    (if (task.priority.isBlank()) "4" else task.priority).toInt()
                }
            }

            listOfNotNull(
                if (overdueTasks.isNotEmpty()) TaskGroup(
                    "Overdue",
                    sortByPriority(overdueTasks),
                    Color.Red
                ) else null,
                if (todayTasks.isNotEmpty()) TaskGroup(
                    "Today",
                    sortByPriority(todayTasks),
                    niceBlueColor
                ) else null,
                if (futureTasks.isNotEmpty()) TaskGroup(
                    "Upcoming",
                    sortByPriority(futureTasks),
                    Color.Gray
                ) else null
            )
        }
    }

    // Clear focus and hide keyboard when switching routes
    LaunchedEffect(currentRoute) {
        if (currentRoute != "search") {
            keyboardController?.hide()
            focusManager.clearFocus()
            searchQuery = "" // Clear search query when leaving search screen
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search bar - only show when current route is "search"
        if (currentRoute == "search") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search tasks...",
                        color = Color(0xFF9E9E9E)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF757575)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF757575)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .focusRequester(searchFocusRequester),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFEEEEEE),
                    unfocusedContainerColor = Color(0xFFEEEEEE),
                    focusedTextColor = Color(0xFF424242),
                    unfocusedTextColor = Color(0xFF424242)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )
        }

        // Task list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (currentRoute == "search" && searchQuery.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start typing to search tasks",
                            color = Color(0xFF9E9E9E),
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (currentRoute == "search" && searchQuery.isNotEmpty() && groupedTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks found for \"$searchQuery\"",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                groupedTasks.forEach { group ->
                    // Group header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(group.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${group.title} (${group.tasks.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = group.color
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    // Group tasks
                    items(group.tasks, key = { task -> "${group.title}_${task.id}" }) { task ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = { dismissValue ->
                                when (dismissValue) {
                                    DismissValue.DismissedToEnd -> {
                                        viewModel.setTaskToUpdate(task)
                                        // Show date picker (do not dismiss)
                                        viewModel.setShowDatePicker(true)
                                        false // prevent item from being swiped off screen
                                    }

                                    DismissValue.DismissedToStart -> {
                                        // Delete the task
                                        viewModel.deleteTask(task.id)
                                        true // allow swipe to delete
                                    }

                                    else -> false
                                }
                            }
                        )

                        Box(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                            SwipeToDismiss(
                                state = dismissState,
                                background = {
                                    val color by animateColorAsState(
                                        targetValue = when (dismissState.targetValue) {
                                            DismissValue.DismissedToStart -> Color.Red
                                            DismissValue.DismissedToEnd -> colorResource(id = R.color.orange)
                                            else -> Color.Transparent
                                        },
                                        label = ""
                                    )

                                    val alignment = when (dismissState.targetValue) {
                                        DismissValue.DismissedToStart -> Alignment.CenterEnd
                                        DismissValue.DismissedToEnd -> Alignment.CenterStart
                                        else -> Alignment.Center
                                    }

                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        when (dismissState.targetValue) {
                                            DismissValue.DismissedToStart -> Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Icon",
                                                tint = Color.White
                                            )

                                            DismissValue.DismissedToEnd -> Icon(
                                                Icons.Default.DateRange,
                                                contentDescription = "Date Icon",
                                                tint = Color.White
                                            )

                                            else -> {}
                                        }
                                    }
                                },
                                directions = setOf(
                                    DismissDirection.EndToStart,
                                    DismissDirection.StartToEnd
                                ),
                                dismissThresholds = { FractionalThreshold(0.1f) },
                                dismissContent = {
                                    TaskItem(task, viewModel) {
                                        // Clear focus and hide keyboard before editing
                                        keyboardController?.hide()
                                        focusManager.clearFocus()

                                        viewModel.setId(task.id)
                                        viewModel.setTaskBeingEdited(true)
                                        viewModel.setShowBottomSheet(true)
                                    }
                                }
                            )
                        }
                    }

                    // Add spacing after each group
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}