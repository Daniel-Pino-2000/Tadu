package com.example.todolist

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskHistoryView(viewModel: TaskViewModel, navController: NavHostController) {

    val taskList = viewModel.getFinishedTasks.collectAsState(initial = listOf())
    val tasks = taskList.value

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
                .padding(innerPadding) // ✅ <-- This fixes the overlap
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(sortedTasks) { index, task ->
                Column {
                    TaskHistoryItem(task = task)

                    if (index < sortedTasks.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun TaskHistoryItem(task: Task) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon - Check mark if completed, trash can if deleted
        Icon(
            imageVector = if (task.isCompleted) Icons.Default.Check else Icons.Default.Delete,
            contentDescription = if (task.isCompleted) "Completed" else "Deleted",
            tint = if (task.isCompleted)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
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

private fun formatDate(timestamp: Long?): String {
    return if (timestamp != null) {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } else {
        "Unknown date"
    }
}
