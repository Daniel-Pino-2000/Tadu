package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.Task
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskCalendarView(
    viewModel: TaskViewModel,
    onTaskClick: (task: Task?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()
    val tasks by viewModel.getPendingTasks.collectAsState(initial = listOf())

    // Group tasks by date using "MMM dd yyyy" as the key
    val tasksByDate = remember(tasks, currentYearMonth) {
        tasks.filter { !it.isDeleted && it.deadline.isNotEmpty() } // Only include tasks with deadlines
            .mapNotNull { task ->
                try {
                    val deadlineDate = parseDeadlineString(task.deadline)
                    deadlineDate?.let { date ->
                        // Use "MMM dd yyyy" as the key for consistency with the rest of the app
                        date.format(DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)) to task
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .groupBy({ it.first }, { it.second })
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Calendar Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                CalendarHeader(
                    currentYearMonth = currentYearMonth,
                    onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) }
                )
            }

            DaysOfWeekHeader()

            // Calendar Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CalendarGrid(
                    yearMonth = currentYearMonth,
                    today = today,
                    selectedDate = selectedDate,
                    tasksByDate = tasksByDate,
                    onDateClick = { date -> selectedDate = date },
                    onTaskClick = onTaskClick
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Selected Day Tasks
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
            ) {
                SelectedDayTasks(
                    selectedDate = selectedDate,
                    tasks = tasksByDate[selectedDate.format(DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH))]
                        ?: emptyList(),
                    onTaskClick = onTaskClick,
                    onTaskDelete = { task -> viewModel.deleteTask(task.id) },
                    onAddTaskClick = {
                        val deadlineString = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH))
                        viewModel.onTaskDeadlineChanged(deadlineString)
                        onTaskClick(null)
                    }
                )
            }
        }
    }
}

// Updated helper function to parse "MMM dd yyyy" deadlines
@RequiresApi(Build.VERSION_CODES.O)
private fun parseDeadlineString(deadline: String): LocalDate? {
    return try {
        val formatter = SimpleDateFormat("MMM d yyyy", Locale.ENGLISH) // handles both single and double-digit days
        val date = formatter.parse(deadline.trim()) ?: return null
        val calendar = Calendar.getInstance().apply { time = date }
        LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    } catch (_: Exception) {
        null
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarHeader(
    currentYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Modern navigation button
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Enhanced month/year display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM")),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = currentYearMonth.year.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Modern navigation button
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    tasksByDate: Map<String, List<Task>>,
    onDateClick: (LocalDate) -> Unit,
    onTaskClick: (Task?) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    // Create list of all calendar cells (empty days + actual days)
    val calendarDays = buildList {
        // Add empty cells for days before the first day of the month
        repeat(firstDayOfWeek) {
            add(null)
        }
        // Add actual days of the month
        for (day in 1..daysInMonth) {
            add(firstDayOfMonth.plusDays(day - 1L))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(calendarDays) { date ->
            CalendarDay(
                date = date,
                isToday = date == today,
                isSelected = date == selectedDate,
                taskCount = date?.let {
                    tasksByDate[it.format(DateTimeFormatter.ofPattern("MMM dd yyyy"))]?.size
                } ?: 0,
                onDateClick = onDateClick,
                modifier = Modifier
                    .aspectRatio(1f)
                    .height(60.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDay(
    date: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean,
    taskCount: Int,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 8.dp else if (isToday) 4.dp else 1.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = date != null) {
                date?.let { onDateClick(it) }
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Day number with modern typography
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    fontSize = 16.sp
                )

                // Task count indicator with modern design - only show if there are tasks with deadlines
                if (taskCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Multiple dots for multiple tasks (max 3 dots)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(minOf(taskCount, 3)) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.secondary,
                                        shape = CircleShape
                                    )
                            )
                        }

                        // Show "+" if more than 3 tasks
                        if (taskCount > 3) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SelectedDayTasks(
    selectedDate: LocalDate,
    tasks: List<Task>,
    onTaskClick: (Task?) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onAddTaskClick: () -> Unit // New parameter for add task with date
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp) // Added more bottom padding
    ) {
        // Compact header with consistent height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Fixed height to prevent compression
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Smaller date indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedDate.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f) // Takes available space
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (tasks.isNotEmpty()) {
                        Text(
                            text = "${tasks.size} ${if (tasks.size == 1) "task" else "tasks"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Add task button - now calls onAddTaskClick to set deadline
                IconButton(
                    onClick = onAddTaskClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add task",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks list with proper spacing and minimum height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 200.dp) // Minimum height to prevent compression
        ) {
            if (tasks.isEmpty()) {
                // Empty state centered in the available space
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No tasks for this day",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 16.dp // Extra bottom padding to prevent cutoff
                    )
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemWithDelete(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onDelete = { onTaskDelete(task) }
                        )
                    }

                    // Add extra spacing after the last item for single task scenarios
                    if (tasks.size == 1) {
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemWithDelete(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    CompactTaskItem(
        task = task,
        onClick = onClick,
        onDeleteClick = { showDeleteDialog = true }
    )

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Task",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${task.title}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CompactTaskItem(
    task: Task,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val priorityColor = PriorityUtils.getCircleColor(
        if (task.priority.isEmpty()) 4 else task.priority.toInt()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 1.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator with completed state
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(
                        color = if (task.isCompleted)
                            priorityColor.copy(alpha = 0.3f)
                        else
                            priorityColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (task.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!task.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Compact priority chip
                        Surface(
                            color = priorityColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (task.priority.isNotEmpty()) task.priority.first().uppercase() else "4",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Compact label
                        if (task.label.isNotEmpty()) {
                            Text(
                                text = task.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

            }

            // Direct delete icon button
            if (onDeleteClick != null) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}