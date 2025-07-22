package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.data.Task
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

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

    // Group tasks by date with improved date parsing - only include tasks with deadlines
    val tasksByDate = remember(tasks) {
        val currentYear = LocalDate.now().year

        tasks.filter { !it.isDeleted && it.deadline.isNotEmpty() } // Only include tasks with deadlines
            .groupBy { task ->
                try {
                    // Parse deadline format like "Jun 5", "Jul 18"
                    val deadlineDate = parseDeadlineString(task.deadline, currentYear)
                    deadlineDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        ?: selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Modern Calendar Header with elevation
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

            // Days of week header with modern styling
            DaysOfWeekHeader()

            // Calendar grid - takes up 55% of the screen with proper padding
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

            // Modern divider with gradient
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

            // Selected day tasks - takes up 45% of the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
            ) {
                SelectedDayTasks(
                    selectedDate = selectedDate,
                    tasks = tasksByDate[selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))] ?: emptyList(),
                    onTaskClick = onTaskClick,
                    onAddTaskClick = {
                        // Set the deadline in the viewModel before triggering add task
                        val deadlineString = selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))
                        viewModel.onTaskDeadlineChanged(deadlineString)
                        onTaskClick(null)
                    }
                )
            }
        }
    }
}

// Helper function to parse deadline strings like "Jun 5", "Jul 18"
@RequiresApi(Build.VERSION_CODES.O)
private fun parseDeadlineString(deadline: String, currentYear: Int): LocalDate? {
    return try {
        // Try parsing formats like "Jun 5", "Jul 18"
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        val date = formatter.parse(deadline)

        if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.YEAR, currentYear)

            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            LocalDate.of(currentYear, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
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
                taskCount = date?.let { tasksByDate[it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))]?.size } ?: 0,
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
                    items(tasks) { task ->
                        CompactTaskItem(
                            task = task,
                            onClick = { onTaskClick(task) }
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
private fun CompactTaskItem(
    task: Task,
    onClick: () -> Unit
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

                // Compact priority and label row
                if (!task.isCompleted && (task.priority.isNotEmpty() || task.label.isNotEmpty())) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Compact priority chip
                        if (task.priority.isNotEmpty()) {
                            Surface(
                                color = priorityColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = task.priority.first().uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = priorityColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Compact label
                        if (task.label.isNotEmpty()) {
                            Text(
                                text = task.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Completion status indicator
            if (task.isCompleted) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}