package com.myapp.tadu

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import com.myapp.tadu.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel, currentRoute: String, undoToastManager: UndoToastManager, onClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(task.isCompleted) }
    val coroutineScope = rememberCoroutineScope()

    val elevationValue = 8.dp // Original elevation

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp), // Original padding
        onClick = { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer, // Visible card in dark mode
        shape = RoundedCornerShape(15.dp), // Original radius
        elevation = elevationValue
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 8.dp), // Original padding
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.width(6.dp)) // Original spacer
                val priority: Int = if (task.priority.isNotEmpty()) {
                    task.priority.toInt()
                } else {
                    4
                }

                CircularCheckbox(
                    checked = isChecked,
                    priority = task.priority,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // Start the animation immediately
                            isChecked = checked

                            coroutineScope.launch {
                                // Wait for animation to complete
                                delay(350)

                                // Show undo toast and complete the task immediately
                                undoToastManager.showTaskCompletedToast(
                                    taskName = task.title,
                                    onComplete = {
                                        // This executes immediately - complete the task
                                        viewModel.completeTask(task.id)
                                    },
                                    onRestore = {
                                        // This executes if user taps undo
                                        isChecked = false // Reset checkbox state
                                        viewModel.restoreTask(task.id) // Your existing restore function
                                    }
                                )
                            }
                        } else {
                            // Handle unchecking if needed
                            isChecked = checked
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp)) // Original spacer

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                end = when {
                                    task.address.isNotEmpty() && task.reminderTime != null -> 48.dp // Both icons
                                    task.address.isNotEmpty() || task.reminderTime != null -> 24.dp // One icon
                                    else -> 0.dp // No icons
                                }
                            )
                    ) {
                        // Title and Label Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = task.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (task.label.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                ModernLabel(
                                    text = task.label,
                                    modifier = Modifier.widthIn(max = 120.dp) // Original width
                                )
                            }
                        }

                        if (task.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp)) // Original spacing
                            Text(
                                modifier = Modifier.padding(start = 3.dp),
                                text = task.description,
                                maxLines = 1, // Original single line
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant // Subtle secondary text
                            )

                            Spacer(modifier = Modifier.height(4.dp)) // Original spacing
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (task.deadline.isNotEmpty()) {
                                DeadlineItem(task, currentRoute)
                            }

                            Spacer(modifier = Modifier.weight(1f)) // Original spacer
                        }
                    }

                    // Icons in the bottom-right corner
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reminder indicator - show first (leftmost)
                        if (task.reminderTime != null) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Has reminder",
                                modifier = Modifier.size(16.dp), // Original size
                                tint = if (task.reminderTime!! <= System.currentTimeMillis()) colorResource(id = R.color.orange)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) // Blue for future reminders
                            )

                            // Add spacing if there's also a location icon
                            if (task.address.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp)) // Original spacing
                            }
                        }

                        // Location indicator - show second (rightmost)
                        if (task.address.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Has address",
                                modifier = Modifier
                                    .size(16.dp), // Original size
                                tint = MaterialTheme.colorScheme.onSurface, // Theme-aware color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), // Much more subtle background like original
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp), // Original padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary, // Accent color text
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            lineHeight = 12.sp, // keeps text height close to font size
            overflow = TextOverflow.Ellipsis
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeadlineItem(task: Task, currentRoute: String) {
    val today = LocalDate.now()
    val currentYear = today.year
    val formatterWithYear = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
    val formatterWithoutYear = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)

    val yesterday = today.minusDays(1)
    val tomorrow = today.plusDays(1)

    // Parse the date – assume task.deadline may or may not have a year
    val parsedDate = try {
        // Try parsing with year first
        LocalDate.parse(task.deadline, formatterWithYear)
    } catch (e: Exception) {
        // If no year is provided, assume current year
        LocalDate.parse("${task.deadline} $currentYear", formatterWithYear)
    }

    val dateStatus: String = when {
        parsedDate.isEqual(today) -> "Today"
        parsedDate.isEqual(yesterday) -> "Yesterday"
        parsedDate.isBefore(today) -> "Past"
        parsedDate.isEqual(tomorrow) -> "Tomorrow"
        else -> "Future"
    }

    val iconColor = when (dateStatus) {
        "Today" -> colorResource(id = R.color.blue_today)
        "Past", "Yesterday" -> colorResource(id = R.color.red_yesterday)
        "Future", "Tomorrow" -> colorResource(id = R.color.green_tomorrow)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // Build display text
    val dateText = when (dateStatus) {
        "Today", "Yesterday", "Tomorrow" -> dateStatus
        else -> {
            if (parsedDate.year != currentYear) {
                parsedDate.format(formatterWithYear) // include year
            } else {
                parsedDate.format(formatterWithoutYear) // omit year
            }
        }
    }

    if (currentRoute != "today" || dateStatus != "Today") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.padding(end = 2.dp))
            Text(
                text = dateText,
                color = iconColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun CircularCheckbox(
    checked: Boolean,
    priority: String = "4",
    onCheckedChange: (Boolean) -> Unit,
) {
    val size: Dp = 23.dp // Original size
    val checkedColor: Color = colorResource(id = R.color.nice_color) // Original color
    val checkmarkColor: Color = Color.White // Original color
    var intPriority = 4

    val hapticFeedback = LocalHapticFeedback.current

    if (priority.isNotEmpty()) {
        intPriority = priority.toInt()
    }

    val borderColor: Color = if (intPriority < 4) {
        PriorityUtils.getBorderColor(intPriority)
    } else {
        Color.Gray // Original fallback
    }

    val uncheckedColor = PriorityUtils.getColor(intPriority) // Original logic

    val border: Dp = if (intPriority < 4) {
        2.dp
    } else {
        1.dp // Original border
    }

    // Smooth scale animation for the whole checkbox
    val checkboxScale by animateFloatAsState(
        targetValue = if (checked) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "checkbox_scale"
    )

    // Background color transition
    val backgroundProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "background_progress"
    )

    // Checkmark animations
    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "checkmark_scale"
    )

    val checkmarkRotation by animateFloatAsState(
        targetValue = if (checked) 0f else -90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkmark_rotation"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(checkboxScale)
            .clip(CircleShape)
            .background(
                // Smooth color interpolation - back to original logic
                Color(
                    red = uncheckedColor.red + (borderColor.red - uncheckedColor.red) * backgroundProgress,
                    green = uncheckedColor.green + (borderColor.green - uncheckedColor.green) * backgroundProgress,
                    blue = uncheckedColor.blue + (borderColor.blue - uncheckedColor.blue) * backgroundProgress,
                    alpha = 1f
                )
            )
            .border(border, borderColor, CircleShape)
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        // Clean checkmark animation
        if (checkmarkScale > 0f) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = checkmarkColor,
                modifier = Modifier
                    .size(size * 0.65f)
                    .scale(checkmarkScale)
                    .rotate(checkmarkRotation)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePicker(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
            val selectedDate = selectedLocalDate.format(formatter)
            onDateSelected(selectedDate)
        },
        year, month, day
    ).show()
}

fun openAddressInMaps(context: Context, address: String) {
    try {
        val encodedLocation = Uri.encode(address.trim())
        val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            context.startActivity(fallbackIntent)
        }
    } catch (e: Exception) {
        Log.e("MapsLaunch", "Failed to open maps", e)
        Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
    }
}