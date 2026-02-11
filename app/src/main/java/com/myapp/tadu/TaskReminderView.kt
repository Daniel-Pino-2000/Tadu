@file:OptIn(ExperimentalMaterial3Api::class)

package com.myapp.tadu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.myapp.tadu.data.Task
import com.myapp.tadu.view_model.TaskViewModel
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

    // Always recompute based on latest data
    val tasksWithReminders = allTasks.filter {
        it.reminderTime != null &&
                it.reminderTime!! > 0 &&
                !it.isDeleted &&
                !it.isCompleted
    }

    // Always recompute groups (no remember)
    val now = System.currentTimeMillis()
    val activeReminders = tasksWithReminders.filter { it.reminderTime!! > now }
    val expiredReminders = tasksWithReminders.filter { it.reminderTime!! <= now }

    var showDeleteDialog by remember { mutableStateOf<Task?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var reminderConfig by remember { mutableStateOf(ReminderConfig()) }
    var backPressed by remember { mutableStateOf(false) }

    // Track the editing task by a stable key (string) so it works if id = Int/Long/UUID
    var editingTaskKey by remember { mutableStateOf<String?>(null) }

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colorResource(id = R.color.dropMenuIcon_gray))
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
            if (tasksWithReminders.isEmpty()) {
                EmptyStateContent(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
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
                        key = { it.id } // keep stable keys
                    ) { task ->
                        MinimalTaskCard(
                            task = task,
                            onEdit = {
                                editingTaskKey = task.id.toString()
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
        val taskToEdit = tasksWithReminders.firstOrNull { it.id.toString() == editingTaskKey }

        AlertDialog(
            onDismissRequest = {
                showReminderDialog = false
                editingTaskKey = null
            },
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

                            taskToEdit?.let { task ->
                                val updatedTask = task.copy(reminderTime = dateTime.timestamp)
                                viewModel.updateTask(updatedTask)
                            }

                            showReminderDialog = false
                            editingTaskKey = null
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
                            editingTaskKey = null
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialog Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = {
                                showReminderDialog = false
                                editingTaskKey = null
                            }
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Show "Remove" if the task currently has a reminder,
                        // not based on reminderConfig.isSet
                        if (taskToEdit?.reminderTime != null) {
                            TextButton(
                                onClick = {
                                    taskToEdit?.let { task ->
                                        val updatedTask = task.copy(
                                            reminderTime = null,
                                            reminderText = null
                                        )
                                        viewModel.updateTask(updatedTask)
                                    }
                                    showReminderDialog = false
                                    editingTaskKey = null
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

    // Relative time text
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

    // Colors (animate to avoid harsh jumps). Keep geometry constant to prevent layout jitter.
    val expiredRed = Color(0xFFDC2626)
    val expiredBackground = MaterialTheme.colorScheme.surfaceContainer
    val expiredBorder = Color(0xFFEF4444)
    val expiredText = Color(0xFF991B1B)

    val priorityColor = if (priority == 4) Color(0xFF212121) else PriorityUtils.getColor(priority)
    val priorityBorderColor = if (priority == 4) Color(0xFF000000) else PriorityUtils.getBorderColor(priority)
    val normalTextColor = MaterialTheme.colorScheme.onSurface

    val targetContainer = if (isExpired) expiredBackground else priorityColor.copy(alpha = 0.04f)
    val targetBorder = if (isExpired) expiredBorder else priorityBorderColor.copy(alpha = 0.4f)
    val targetPrimaryText = if (isExpired) expiredText else normalTextColor
    val targetAccent = if (isExpired) expiredRed else priorityBorderColor

    val containerColor by animateColorAsState(targetValue = targetContainer, animationSpec = tween(200))
    val borderColor by animateColorAsState(targetValue = targetBorder, animationSpec = tween(200))
    val primaryTextColor by animateColorAsState(targetValue = targetPrimaryText, animationSpec = tween(200))
    val accentColor by animateColorAsState(targetValue = targetAccent, animationSpec = tween(200))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            // Keep width constant (no thickness change) to avoid layout jumps
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .animateContentSize(), // smooth content size changes
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                        color = primaryTextColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Label if available
                    if (task.label.isNotEmpty()) {
                        Surface(
                            color = if (isExpired)
                                Color(0xFFEF4444).copy(alpha = 0.15f)
                            else
                                priorityColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = task.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
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
                    // Time until/since reminder with distinct expired styling
                    Surface(
                        color = if (isExpired)
                            Color(0xFFDC2626).copy(alpha = 0.1f)
                        else
                            Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isExpired) Color(0xFFDC2626) else accentColor,
                            fontWeight = if (isExpired) FontWeight.Bold else FontWeight.SemiBold,
                            modifier = if (isExpired)
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            else
                                Modifier
                        )
                    }

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Exact reminder time
                    Text(
                        text = timeFormatter.format(Date(task.reminderTime!!)),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isExpired)
                            Color(0xFF991B1B)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Description (only if short and relevant for reminders)
                if (!task.description.isNullOrEmpty() && task.description.length <= 50) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpired)
                            Color(0xFF991B1B).copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
                        tint = accentColor
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
                        tint = if (isExpired)
                            Color(0xFFDC2626)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Helper data class for multiple return values (kept, though no longer used)
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

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
    val isSet: Boolean = false // not used for logic anymore
)
