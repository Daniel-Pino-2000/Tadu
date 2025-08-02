@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.todolist.data.Task
import java.text.SimpleDateFormat
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskRemindersScreen(
    viewModel: TaskViewModel,
    navController: NavHostController
) {
    val allTasks by viewModel.getTasksWithReminders.collectAsStateWithLifecycle(emptyList())
    val tasksWithReminders = remember(allTasks) {
        allTasks.filter {
            it.reminderTime != null &&
                    it.reminderTime!! > 0 &&
                    !it.isDeleted &&
                    !it.isCompleted
        }
    }

    val activeReminders = remember(tasksWithReminders) {
        tasksWithReminders.filter { it.reminderTime!! > System.currentTimeMillis() }
    }

    val expiredReminders = remember(tasksWithReminders) {
        tasksWithReminders.filter { it.reminderTime!! <= System.currentTimeMillis() }
    }

    var showDeleteDialog by remember { mutableStateOf<Task?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var reminderConfig by remember { mutableStateOf(ReminderConfig()) }
    var backPressed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!backPressed) {
                                backPressed = true
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                tasksWithReminders.isEmpty() -> {
                    EmptyStateContent(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activeReminders.isNotEmpty() || expiredReminders.isNotEmpty()) {
                            item {
                                StatusSummary(
                                    activeCount = activeReminders.size,
                                    expiredCount = expiredReminders.size
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        items(
                            items = tasksWithReminders.sortedBy { it.reminderTime },
                            key = { it.id }
                        ) { task ->
                            MinimalTaskCard(
                                task = task,
                                onEdit = {
                                    reminderConfig = reminderConfig.copy(
                                        enabled = true,
                                        dateTime = ReminderDateTime().apply {
                                            timestamp = task.reminderTime ?: System.currentTimeMillis()
                                        }
                                    )
                                    showReminderDialog = true
                                },
                                onDelete = { showDeleteDialog = task },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { task ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remove Reminder?") },
            text = { Text("Remove reminder for \"${task.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedTask = task.copy(
                            reminderTime = null,
                            reminderText = null
                        )
                        viewModel.updateTask(updatedTask)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit reminder dialog
    if (showReminderDialog) {
        val taskToEdit = tasksWithReminders.firstOrNull { it.reminderTime == reminderConfig.dateTime.timestamp }

        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Edit Reminder") },
            text = {
                Column {
                    taskToEdit?.let { task ->
                        Text(
                            text = "Task: ${task.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Your custom Date/Time Picker
                    ReminderDateTimePicker(
                        initialDateTime = reminderConfig.dateTime,
                        onReminderSet = { dateTime ->
                            reminderConfig = reminderConfig.copy(
                                enabled = true,
                                dateTime = dateTime
                            )

                            // Update the task
                            taskToEdit?.let { task ->
                                val updatedTask = task.copy(reminderTime = dateTime.timestamp)
                                viewModel.updateTask(updatedTask)
                            }

                            showReminderDialog = false
                        },
                        onReminderCleared = {
                            reminderConfig = reminderConfig.copy(
                                enabled = false,
                                dateTime = ReminderDateTime()
                            )

                            taskToEdit?.let { task ->
                                val updatedTask = task.copy(
                                    reminderTime = null,
                                    reminderText = null
                                )
                                viewModel.updateTask(updatedTask)
                            }

                            showReminderDialog = false
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialog Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = { showReminderDialog = false }
                        ) {
                            Text(
                                text = "Cancel",
                                color = androidx.compose.ui.graphics.Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (reminderConfig.enabled && reminderConfig.dateTime.isSet) {
                            TextButton(
                                onClick = {
                                    reminderConfig = reminderConfig.copy(
                                        enabled = false,
                                        dateTime = ReminderDateTime()
                                    )

                                    taskToEdit?.let { task ->
                                        val updatedTask = task.copy(
                                            reminderTime = null,
                                            reminderText = null
                                        )
                                        viewModel.updateTask(updatedTask)
                                    }

                                    showReminderDialog = false
                                }
                            ) {
                                Text(
                                    text = "Remove",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun StatusSummary(
    activeCount: Int,
    expiredCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        if (activeCount > 0) {
            Text(
                text = "$activeCount active",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        if (activeCount > 0 && expiredCount > 0) {
            Text(
                text = " • ",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (expiredCount > 0) {
            Text(
                text = "$expiredCount expired",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MinimalTaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
    val isExpired = task.reminderTime!! <= System.currentTimeMillis()

    val priority = task.priority.toIntOrNull() ?: 4

    val priorityColor = if (priority == 4) Color(0xFF212121) else PriorityUtils.getColor(priority) // Dark Gray
    val priorityBorderColor = if (priority == 4) Color(0xFF000000) else PriorityUtils.getBorderColor(priority) // Black




    // Calculate time difference
    val timeText = remember(task.reminderTime) {
        val diff = task.reminderTime!! - System.currentTimeMillis()
        when {
            diff <= 0 -> "Expired"
            diff < 60 * 1000 -> "Now"
            diff < 60 * 60 * 1000 -> "${(diff / (60 * 1000)).toInt()}m"
            diff < 24 * 60 * 60 * 1000 -> "${(diff / (60 * 60 * 1000)).toInt()}h"
            else -> "${(diff / (24 * 60 * 60 * 1000)).toInt()}d"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isExpired)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                else
                    priorityBorderColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)
            else
                priorityColor.copy(alpha = 0.04f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Task info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title and label row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isExpired)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, false)
                    )

                    // Label if available
                    if (task.label.isNotEmpty()) {
                        Surface(
                            color = priorityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = task.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityBorderColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Time info row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time until/since reminder
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isExpired)
                            MaterialTheme.colorScheme.error
                        else
                            priorityBorderColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Exact reminder time
                    Text(
                        text = timeFormatter.format(Date(task.reminderTime!!)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Description (only if short and relevant for reminders)
                if (!task.description.isNullOrEmpty() && task.description.length <= 50) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }

            // Right side - Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp),
                        tint = if (isExpired)
                            MaterialTheme.colorScheme.error
                        else
                            priorityBorderColor
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No reminders set",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Tasks with reminders will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Data classes for reminder configuration
data class ReminderConfig(
    val enabled: Boolean = false,
    val dateTime: ReminderDateTime = ReminderDateTime()
)

data class ReminderDateTime(
    var timestamp: Long = 0L,
    val date: String = "",
    val time: String = "",
    val isSet: Boolean = false
)