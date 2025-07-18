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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()
    val tasks by viewModel.getAllTasks.collectAsState(initial = listOf())

    // Group tasks by date with improved date parsing
    val tasksByDate = remember(tasks) {
        val currentYear = LocalDate.now().year

        tasks.filter { !it.isDeleted }
            .groupBy { task ->
                if (task.deadline.isNotEmpty()) {
                    try {
                        // Parse deadline format like "Jun 5", "Jul 18"
                        val deadlineDate = parseDeadlineString(task.deadline, currentYear)
                        deadlineDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            ?: selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } catch (e: Exception) {
                        selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                } else {
                    selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Calendar Header
        CalendarHeader(
            currentYearMonth = currentYearMonth,
            onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
            onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) }
        )

        // Days of week header
        DaysOfWeekHeader()

        // Calendar grid - takes up 60% of the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
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
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        // Selected day tasks - takes up 40% of the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
        ) {
            SelectedDayTasks(
                selectedDate = selectedDate,
                tasks = tasksByDate[selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))] ?: emptyList(),
                onTaskClick = onTaskClick
            )
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
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous month"
            )
        }

        Text(
            text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next month"
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    onTaskClick: (Task) -> Unit
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
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    .height(80.dp)
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
    Box(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { date?.let { onDateClick(it) } }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Day number
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Task count indicator
                if (taskCount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (taskCount > 9) "9+" else taskCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Bold
                        )
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
    onTaskClick: (Task) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Tasks for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks list
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks for this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    DetailedTaskItem(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedTaskItem(
    task: Task,
    onClick: () -> Unit
) {
    val priorityColor = when (task.priority.lowercase()) {
        "high" -> Color(0xFFE53E3E)
        "medium" -> Color(0xFFFF9800)
        "low" -> Color(0xFF38A169)
        else -> Color(0xFF718096)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = priorityColor,
                        shape = CircleShape
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
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Priority and label row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${task.priority} Priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )

                    if (task.label.isNotEmpty()) {
                        Text(
                            text = task.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Usage example in your screen/activity
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    viewModel: TaskViewModel,
    onTaskClick: (Task) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TaskCalendarView(
            viewModel = viewModel,
            onTaskClick = onTaskClick
        )
    }
}