package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.text.SimpleDateFormat
import java.util.*

data class ReminderConfig(
    val enabled: Boolean = false,
    val type: ReminderType = ReminderType.PRESET,
    val presetTime: String = "15 minutes before",
    val customDateTime: LocalDateTime? = null
)

enum class ReminderType {
    PRESET, CUSTOM
}

// Updated to handle String deadline from database
@RequiresApi(Build.VERSION_CODES.O)
fun ReminderConfig.getTriggerTime(taskDeadlineString: String): LocalDateTime? {
    return try {
        if (!enabled) return null

        // Convert string deadline to LocalDateTime
        val taskDeadline = parseDeadlineString(taskDeadlineString)
            ?: return null

        when (type) {
            ReminderType.CUSTOM -> customDateTime
            ReminderType.PRESET -> {
                val offset = when (presetTime) {
                    "5 minutes before" -> Duration.ofMinutes(5)
                    "15 minutes before" -> Duration.ofMinutes(15)
                    "30 minutes before" -> Duration.ofMinutes(30)
                    "1 hour before" -> Duration.ofHours(1)
                    "1 day before" -> Duration.ofDays(1)
                    else -> Duration.ZERO
                }
                taskDeadline.minus(offset)
            }
        }
    } catch (e: Exception) {
        null
    }
}

// Helper function to parse the deadline string from database
@RequiresApi(Build.VERSION_CODES.O)
private fun parseDeadlineString(deadlineString: String): LocalDateTime? {
    return try {
        // Assuming the deadline is stored as Date().toString()
        // This typically produces format like "Wed Oct 05 14:28:03 GMT 2024"
        val date = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            .parse(deadlineString)

        date?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            LocalDateTime.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // Calendar.MONTH is 0-based
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        }
    } catch (e: Exception) {
        // Fallback: try other common formats
        try {
            // Try ISO format as fallback
            LocalDateTime.parse(deadlineString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e2: Exception) {
            null
        }
    }
}

// Data class to return reminder results
data class ReminderResult(
    val config: ReminderConfig,
    val triggerTime: LocalDateTime?,
    val isValid: Boolean
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTriggerTimeMillis(): Long? {
        return triggerTime?.let {
            val calendar = Calendar.getInstance()
            calendar.set(
                it.year,
                it.monthValue - 1, // Calendar months are 0-based
                it.dayOfMonth,
                it.hour,
                it.minute,
                0
            )
            calendar.timeInMillis
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    modifier: Modifier = Modifier,
    onReminderChange: (ReminderResult) -> Unit, // Changed to return ReminderResult
    initialConfig: ReminderConfig = ReminderConfig(),
    taskDeadline: String // Changed to String to match your database field
) {
    val context = LocalContext.current
    var reminderConfig by remember(initialConfig, taskDeadline) {
        mutableStateOf(initialConfig)
    }

    // Update reminderConfig when initialConfig changes and call the callback
    LaunchedEffect(initialConfig) {
        if (reminderConfig != initialConfig) {
            reminderConfig = initialConfig
            val triggerTime = initialConfig.getTriggerTime(taskDeadline)
            val result = ReminderResult(
                config = initialConfig,
                triggerTime = triggerTime,
                isValid = triggerTime != null && initialConfig.enabled
            )
            onReminderChange(result)
        }
    }
    var showCustomDialog by remember { mutableStateOf(false) }

    // Function to calculate and return reminder result
    fun updateReminderConfig(newConfig: ReminderConfig) {
        reminderConfig = newConfig
        val triggerTime = newConfig.getTriggerTime(taskDeadline)
        val result = ReminderResult(
            config = newConfig,
            triggerTime = triggerTime,
            isValid = triggerTime != null && newConfig.enabled
        )
        onReminderChange(result)
    }

    // Launcher to open the exact alarm permission screen
    val alarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // After user returns from permission screen
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            updateReminderConfig(reminderConfig.copy(enabled = true))
        }
    }

    // Parse deadline for display
    val parsedDeadline = remember(taskDeadline) {
        parseDeadlineString(taskDeadline)
    }

    // Always show the reminder section
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminderConfig.enabled) {
                colorResource(id = R.color.nice_blue).copy(alpha = 0.06f)
            } else {
                Color.Gray.copy(alpha = 0.03f)
            }
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (reminderConfig.enabled) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                colorResource(id = R.color.nice_blue).copy(alpha = 0.2f)
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header row with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Reminder",
                    tint = if (reminderConfig.enabled)
                        colorResource(id = R.color.nice_blue)
                    else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reminder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (reminderConfig.enabled)
                            colorResource(id = R.color.nice_blue)
                        else Color.Black
                    )

                    if (reminderConfig.enabled) {
                        val displayText = when (reminderConfig.type) {
                            ReminderType.PRESET -> reminderConfig.presetTime
                            ReminderType.CUSTOM -> reminderConfig.customDateTime?.let {
                                "Custom: ${it.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}"
                            } ?: "Custom time"
                        }

                        // Show trigger time if available
                        val triggerTime = reminderConfig.getTriggerTime(taskDeadline)
                        val finalDisplayText = if (triggerTime != null) {
                            "$displayText (${triggerTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))})"
                        } else {
                            displayText
                        }

                        Text(
                            text = finalDisplayText,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Get notified before deadline",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Switch(
                    checked = reminderConfig.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                !alarmManager.canScheduleExactAlarms()
                            ) {
                                // Request permission by opening settings
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                alarmPermissionLauncher.launch(intent)
                            } else {
                                updateReminderConfig(reminderConfig.copy(enabled = true))
                            }
                        } else {
                            updateReminderConfig(reminderConfig.copy(enabled = false))
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(id = R.color.nice_blue),
                        checkedTrackColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.scale(0.9f)
                )
            }

            // Time selection when reminder is enabled
            if (reminderConfig.enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presetOptions = listOf(
                        "5 min" to "5 minutes before",
                        "15 min" to "15 minutes before",
                        "30 min" to "30 minutes before",
                        "1 hour" to "1 hour before",
                        "1 day" to "1 day before"
                    )

                    // Preset time options
                    items(presetOptions) { (shortLabel, fullValue) ->
                        val isSelected = reminderConfig.type == ReminderType.PRESET &&
                                reminderConfig.presetTime == fullValue

                        FilterChip(
                            onClick = {
                                updateReminderConfig(
                                    reminderConfig.copy(
                                        type = ReminderType.PRESET,
                                        presetTime = fullValue
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text = shortLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            selected = isSelected,
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorResource(id = R.color.nice_blue),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color.Gray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.Gray.copy(alpha = 0.3f),
                                selectedBorderColor = colorResource(id = R.color.nice_blue)
                            )
                        )
                    }

                    // Custom option
                    item {
                        val isCustomSelected = reminderConfig.type == ReminderType.CUSTOM

                        FilterChip(
                            onClick = {
                                showCustomDialog = true
                            },
                            label = {
                                Text(
                                    text = "Custom",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            selected = isCustomSelected,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorResource(id = R.color.nice_blue),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color.Gray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isCustomSelected,
                                borderColor = Color.Gray.copy(alpha = 0.3f),
                                selectedBorderColor = colorResource(id = R.color.nice_blue)
                            )
                        )
                    }
                }
            }
        }
    }

    // Custom reminder dialog
    if (showCustomDialog) {
        CustomReminderDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { dateTime ->
                updateReminderConfig(
                    reminderConfig.copy(
                        type = ReminderType.CUSTOM,
                        customDateTime = dateTime
                    )
                )
                showCustomDialog = false
            },
            initialDateTime = reminderConfig.customDateTime,
            taskDeadline = parsedDeadline // Pass parsed deadline for validation
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit,
    initialDateTime: LocalDateTime? = null,
    taskDeadline: LocalDateTime? = null // Add task deadline for validation
) {
    var selectedDate by remember {
        mutableStateOf(initialDateTime?.toLocalDate() ?: java.time.LocalDate.now())
    }
    var selectedTime by remember {
        mutableStateOf(initialDateTime?.toLocalTime() ?: java.time.LocalTime.of(9, 0))
    }
    var currentStep by remember { mutableStateOf(0) } // 0 = date, 1 = time
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentStep == 0) "Select Date" else "Select Time",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.nice_blue)
                        )
                        if (showError) {
                            Text(
                                text = "Reminder time must be before deadline",
                                fontSize = 10.sp,
                                color = Color.Red
                            )
                        }
                    }

                    // Step indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date step indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (currentStep == 0)
                                        colorResource(id = R.color.nice_blue)
                                    else
                                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )

                        // Connection line
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(
                                    color = colorResource(id = R.color.nice_blue).copy(alpha = 0.3f)
                                )
                        )

                        // Time step indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (currentStep == 1)
                                        colorResource(id = R.color.nice_blue)
                                    else
                                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary card showing current selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Reminder set for:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.nice_blue)
                        )
                        Text(
                            text = selectedTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.nice_blue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on current step
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Fixed height to prevent layout shifts
                ) {
                    if (currentStep == 0) {
                        // Date Picker
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        )

                        // Update selectedDate when date picker changes
                        LaunchedEffect(datePickerState.selectedDateMillis) {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        }

                        Column {
                            DatePicker(
                                state = datePickerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp), // Add bottom padding to prevent overlap
                                showModeToggle = false,
                                colors = DatePickerDefaults.colors(
                                    selectedDayContainerColor = colorResource(id = R.color.nice_blue),
                                    todayDateBorderColor = colorResource(id = R.color.nice_blue)
                                )
                            )
                        }
                    } else {
                        // Time Picker
                        val timePickerState = rememberTimePickerState(
                            initialHour = selectedTime.hour,
                            initialMinute = selectedTime.minute,
                            is24Hour = false
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TimePickerDefaults.colors(
                                    selectorColor = colorResource(id = R.color.nice_blue),
                                    containerColor = Color.White,
                                    periodSelectorSelectedContainerColor = colorResource(id = R.color.nice_blue),
                                    periodSelectorUnselectedContainerColor = Color.Gray.copy(alpha = 0.1f),
                                    periodSelectorSelectedContentColor = Color.White,
                                    timeSelectorSelectedContainerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.2f),
                                    timeSelectorSelectedContentColor = colorResource(id = R.color.nice_blue)
                                )
                            )

                            // Update button to apply time changes
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    selectedTime = java.time.LocalTime.of(
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Update Time",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorResource(id = R.color.nice_blue)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Next/Confirm button
                    Button(
                        onClick = {
                            if (currentStep == 0) {
                                currentStep = 1
                                showError = false
                            } else {
                                val selectedDateTime = selectedDate.atTime(selectedTime)

                                // Validate against task deadline
                                if (taskDeadline != null && selectedDateTime.isAfter(taskDeadline)) {
                                    showError = true
                                    return@Button
                                }

                                onConfirm(selectedDateTime)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.nice_blue)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentStep == 0) "Next →" else "Set Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Back button on time step
                if (currentStep == 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                currentStep = 0
                                showError = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorResource(id = R.color.nice_blue)
                            )
                        ) {
                            Text(
                                text = "← Back to Date",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// Usage example:
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExampleUsage() {
    val taskDeadline = "Wed Oct 05 14:28:03 GMT 2024" // Your database format

    ReminderSection(
        taskDeadline = taskDeadline,
        onReminderChange = { result ->
            // Handle the reminder result
            if (result.isValid) {
                println("Reminder set for: ${result.triggerTime}")
                println("Trigger time millis: ${result.getTriggerTimeMillis()}")

                // You can now use result.getTriggerTimeMillis() to schedule an alarm
                // or store the reminder configuration
            }
        }
    )
}