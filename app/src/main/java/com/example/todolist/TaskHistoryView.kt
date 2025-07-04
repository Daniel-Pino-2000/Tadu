package com.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.todolist.data.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskHistoryView(viewModel: TaskViewModel, navController: NavHostController) {

    val taskList = viewModel.getFinishedTasks.collectAsState(initial = listOf())
    val tasks = taskList.value

    // Sort tasks by completion/deletion date (most recent first)
    val sortedTasks = tasks.sortedByDescending { task ->
        task.completionDate ?: task.deletionDate
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedTasks) { task ->
            TaskHistoryItem(task = task)
        }
    }
}

@Composable
fun TaskHistoryItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon - Check mark if completed, trash can if deleted
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.Check else Icons.Default.Delete,
                contentDescription = if (task.isCompleted) "Completed" else "Deleted",
                tint = if (task.isCompleted) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Task title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                val dateText = formatDate(task.completionDate ?: task.deletionDate)
                Text(
                    text = if (task.isCompleted) "Completed: $dateText" else "Deleted: $dateText",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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