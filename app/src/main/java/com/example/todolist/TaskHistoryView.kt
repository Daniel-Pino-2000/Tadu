package com.example.todolist

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.todolist.data.Task
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskHistoryView(viewModel: TaskViewModel, navController: NavHostController) {

    val taskList = viewModel.getFinishedTasks.collectAsState(initial = listOf())
    val tasks = taskList.value

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // Observe UI state for keyboard management
    val uiState by viewModel.uiState.collectAsState()

    val selectedTaskId = uiState.currentId
    val selectedTask by viewModel.getTaskById(selectedTaskId).collectAsState(initial = null)



    // Sort tasks by completion/deletion date (most recent first)
    val sortedTasks = tasks.sortedByDescending { task ->
        task.completionDate ?: task.deletionDate
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Task History",
                        color = colorResource(id = R.color.black),
                        style = MaterialTheme.typography.titleLarge, // Larger and bolder by default
                        modifier = Modifier.padding(start = 8.dp)
                    )

                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },


                )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // ✅ <-- This fixes the overlap
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(sortedTasks) { index, task ->
                Column {
                    TaskHistoryItem(task, viewModel) {
                        viewModel.setId(task.id)
                        viewModel.setTaskBeingEdited(true)
                        viewModel.setShowBottomSheet(true)
                    }

                    if (index < sortedTasks.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Show bottom sheet UI conditionally:
        if (uiState.showBottomSheet) {
            AddTaskView(
                id = uiState.currentId,
                viewModel = viewModel,
                onDismiss = {
                    viewModel.setShowBottomSheet(false)
                    viewModel.setTaskBeingEdited(false)
                },
                onSubmit = { task ->
                    // In history mode, we restore the task (make it active again)
                    viewModel.restoreTask(task.id) // You'll need to implement this method
                    viewModel.setShowBottomSheet(false)
                    viewModel.setTaskBeingEdited(false)
                },
                isHistoryMode = true, // This is the key difference
                onDelete = { taskId ->
                    selectedTask?.let { task ->
                        viewModel.permanentlyDeleteTask(task)
                    }
                }
            )
        }
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskHistoryItem(task: Task, viewModel: TaskViewModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Icon(
            imageVector = if (task.isCompleted) Icons.Default.Check else Icons.Default.Delete,
            contentDescription = if (task.isCompleted) "Completed" else "Deleted",
            tint = if (task.isCompleted)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val dateText = formatDate(task.completionDate ?: task.deletionDate)
            Text(
                text = if (task.isCompleted) "Completed: $dateText" else "Deleted: $dateText",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Priority chip
        val priorityInt = task.priority?.toIntOrNull() ?: 4
        val priorityColor = PriorityUtils.getCircleColor(priorityInt)
        val priorityLabel = when (priorityInt) {
            1 -> "High"
            2 -> "Medium"
            3 -> "Low"
            4 -> "Normal" // 👈 Added priority 4
            else -> "N/A"
        }

        if (priorityLabel != "N/A") {
            Box(
                modifier = Modifier
                    .background(priorityColor.copy(alpha = 0.2f), shape = CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = priorityLabel,
                    fontSize = 12.sp,
                    color = priorityColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



private fun formatDate(timestamp: Long?): String {
    return if (timestamp != null) {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } else {
        "Unknown date"
    }
}