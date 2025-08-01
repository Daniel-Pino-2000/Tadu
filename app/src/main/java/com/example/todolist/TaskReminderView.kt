package com.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit

data class ReminderConfig(
    val enabled: Boolean = false,
    val dateTime: ReminderDateTime = ReminderDateTime()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    modifier: Modifier = Modifier,
    initialReminder: Long? = null,
    onReminderChanged: (reminderTime: Long?, reminderText: String?) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity

    var reminderConfig by remember {
        mutableStateOf(
            ReminderConfig(
                enabled = initialReminder != null,
                dateTime = initialReminder?.toReminderDateTime() ?: ReminderDateTime()
            )
        )
    }

    // Update reminderConfig when initialReminder changes (when task data loads)
    LaunchedEffect(initialReminder) {
        reminderConfig = ReminderConfig(
            enabled = initialReminder != null,
            dateTime = initialReminder?.toReminderDateTime() ?: ReminderDateTime()
        )
    }

    // State for controlling the reminder picker dialog
    var showReminderDialog by remember { mutableStateOf(false) }

    // Compact Main Reminder Card - reduced padding and size
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
        shape = RoundedCornerShape(12.dp), // Slightly smaller corner radius
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (reminderConfig.enabled) {
            BorderStroke(
                1.dp,
                colorResource(id = R.color.nice_blue).copy(alpha = 0.2f)
            )
        } else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) { // Reduced from 18dp to 14dp
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
                    modifier = Modifier.size(18.dp) // Reduced from 20dp
                )

                Spacer(modifier = Modifier.width(10.dp)) // Reduced from 12dp

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reminder",
                        fontSize = 15.sp, // Reduced from 16sp
                        fontWeight = FontWeight.Medium,
                        color = if (reminderConfig.enabled)
                            colorResource(id = R.color.nice_blue)
                        else Color.Black
                    )
                    Text(
                        text = when {
                            reminderConfig.enabled && reminderConfig.dateTime.isSet ->
                                "${reminderConfig.dateTime.date} at ${reminderConfig.dateTime.time}"
                            reminderConfig.enabled ->
                                "Tap to set date and time"
                            else ->
                                "Get notified before deadline"
                        },
                        fontSize = 12.sp, // Reduced from 13sp
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 1.dp) // Reduced from 2dp
                    )
                }

                Switch(
                    checked = reminderConfig.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Request permissions when user enables reminders
                            activity?.requestRequiredPermissions()
                            // Just enable the reminder, don't automatically open dialog
                            reminderConfig = reminderConfig.copy(enabled = true)
                        } else {
                            // Clear reminder when disabled
                            reminderConfig = reminderConfig.copy(
                                enabled = false,
                                dateTime = ReminderDateTime()
                            )
                            onReminderChanged(null, null)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorResource(id = R.color.nice_blue),
                        checkedTrackColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.scale(0.85f) // Slightly smaller switch
                )
            }

            // Compact reminder status - only show when enabled and set
            if (reminderConfig.enabled && reminderConfig.dateTime.isSet) {
                Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16dp

                // Single compact chip showing reminder is active
                AssistChip(
                    onClick = { showReminderDialog = true },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp), // Smaller icon
                                tint = colorResource(id = R.color.nice_blue)
                            )
                            Text(
                                "Active • Tap to edit",
                                fontSize = 12.sp, // Smaller text
                                color = colorResource(id = R.color.nice_blue)
                            )
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.height(28.dp) // Compact height
                )
            } else if (reminderConfig.enabled && !reminderConfig.dateTime.isSet) {
                // Compact button to set the reminder when enabled but not set
                Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

                TextButton(
                    onClick = { showReminderDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp), // Reduced height
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(id = R.color.nice_blue)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Set Reminder Time",
                        fontSize = 13.sp, // Smaller text
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Compact Reminder Picker Dialog
    if (showReminderDialog) {
        Dialog(onDismissRequest = { showReminderDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp), // Add horizontal padding for smaller dialog
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp), // Reduced corner radius
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp) // Reduced padding
                    ) {
                        // Compact Dialog Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Set Reminder",
                                tint = colorResource(id = R.color.nice_blue),
                                modifier = Modifier.size(20.dp) // Smaller icon
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Set Reminder",
                                fontSize = 18.sp, // Reduced from 20sp
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Reduced spacing

                        // Compact Date/Time Picker
                        ReminderDateTimePicker(
                            initialDateTime = reminderConfig.dateTime,
                            onReminderSet = { dateTime ->
                                reminderConfig = reminderConfig.copy(
                                    enabled = true,
                                    dateTime = dateTime
                                )

                                // Return the timestamp and display text
                                val reminderText = "Set for ${dateTime.date} at ${dateTime.time}"
                                onReminderChanged(dateTime.timestamp, reminderText)

                                // Close the dialog
                                showReminderDialog = false
                            },
                            onReminderCleared = {
                                reminderConfig = reminderConfig.copy(
                                    enabled = false,
                                    dateTime = ReminderDateTime()
                                )
                                onReminderChanged(null, null)
                                showReminderDialog = false
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp)) // Reduced spacing

                        // Compact Dialog Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End) // Reduced spacing
                        ) {
                            TextButton(
                                onClick = { showReminderDialog = false }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Gray,
                                    fontSize = 13.sp, // Smaller text
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
                                        onReminderChanged(null, null)
                                        showReminderDialog = false
                                    }
                                ) {
                                    Text(
                                        text = "Remove",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 13.sp, // Smaller text
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}