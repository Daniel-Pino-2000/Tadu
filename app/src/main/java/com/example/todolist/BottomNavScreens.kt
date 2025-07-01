package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
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

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
    val currentYear = today.year

    val taskList = viewModel.getAllTasks.collectAsState(initial = listOf())
    val tasks = taskList.value

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

    val groupedTasks = remember(tasksToDisplay, today) {
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
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
                                viewModel.deleteTask(task)
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

