package com.example.todolist

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.todolist.data.Task
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeView(navController: NavHostController, viewModel: TaskViewModel) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val taskToUpdate = remember { mutableStateOf(Task(0, "", "", "", "4", "")) } // Dummy task

    var id by remember { mutableLongStateOf(0L) }

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = { AppBarView(title = "Today") },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colorResource(id = R.color.nice_blue),
                onClick = {
                    taskBeingEdited = false
                    showBottomSheet = true
                    id = 0L
                }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }

    ) {
        val taskList = viewModel.getAllTasks.collectAsState(initial = listOf())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            // Sorts the tasks by priority
            val sortedTasks = taskList.value.sortedBy {
                (if (it.priority.isBlank()) "4" else it.priority).toInt()
            }


            items(sortedTasks, key = { task -> task.id }) { task ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        when (dismissValue) {
                            DismissValue.DismissedToEnd -> {
                                taskToUpdate.value = task
                                // Show date picker (do not dismiss)
                                showDatePicker.value = true
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

                Box(modifier = Modifier.padding(top = 8.dp)) {
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
                                id = task.id
                                taskBeingEdited = true
                                showBottomSheet = true
                            }
                        }
                    )
                }
            }

        }

    }

    if (showDatePicker.value) {
        DatePicker { selectedDate ->
            viewModel.onTaskDeadlineChanged(selectedDate)
            val newTask = Task(
                id = taskToUpdate.value.id,
                title = taskToUpdate.value.title,
                description = taskToUpdate.value.description,
                date = taskToUpdate.value.date,
                address = taskToUpdate.value.address,
                priority = taskToUpdate.value.priority,
                deadline = selectedDate
            )
            viewModel.updateTask(newTask) // When the user picks a date, update the task:
        }
        showDatePicker.value = false
    }

    if (showBottomSheet) {
        AddTaskView(
            id,
            viewModel,
            onDismiss = {
                showBottomSheet = false
                taskBeingEdited = false
            },
            onSubmit = { task ->
                if (taskBeingEdited == false) {
                    viewModel.addTask(task)  // ← Add to DB
                } else {
                    viewModel.updateTask(task)  // ← Update in DB

                }
                showBottomSheet = false
                taskBeingEdited = false

            }
        )
    }
}