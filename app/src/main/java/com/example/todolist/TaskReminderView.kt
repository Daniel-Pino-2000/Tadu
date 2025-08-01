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

    // Main Reminder Toggle Card - this is the only thing that shows in the bottom sheet
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
            BorderStroke(
                1.dp,
                colorResource(id = R.color.nice_blue).copy(alpha = 0.2f)
            )
        } else null
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                    Text(
                        text = when {
                            reminderConfig.enabled && reminderConfig.dateTime.isSet ->
                                "Set for ${reminderConfig.dateTime.date} at ${reminderConfig.dateTime.time}"
                            reminderConfig.enabled ->
                                "Tap to set date and time"
                            else ->
                                "Get notified before deadline"
                        },
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
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
                    modifier = Modifier.scale(0.9f)
                )
            }

            // Show active reminder chip when enabled and set - now clickable to edit
            if (reminderConfig.enabled && reminderConfig.dateTime.isSet) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(1) {
                        AssistChip(
                            onClick = {
                                // Open dialog to edit the reminder
                                showReminderDialog = true
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = colorResource(id = R.color.nice_blue)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Reminder active • Tap to edit",
                                        color = colorResource(id = R.color.nice_blue)
                                    )
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            } else if (reminderConfig.enabled && !reminderConfig.dateTime.isSet) {
                // Show a button to set the reminder when enabled but not set
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showReminderDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(id = R.color.nice_blue)
                    ),
                    border = BorderStroke(
                        1.dp,
                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Set Reminder Time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Reminder Picker Dialog - this is where the date/time picker now lives
    if (showReminderDialog) {
        Dialog(onDismissRequest = { showReminderDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000)), // Semi-transparent backdrop
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Dialog Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Set Reminder",
                                tint = colorResource(id = R.color.nice_blue),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Set Reminder",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Date/Time Picker - now has all the space it needs
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dialog Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            TextButton(
                                onClick = { showReminderDialog = false }
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
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
                                        fontSize = 14.sp,
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