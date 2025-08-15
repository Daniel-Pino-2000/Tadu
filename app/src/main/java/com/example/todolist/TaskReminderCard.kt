package com.example.todolist

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.core.content.ContextCompat


enum class ReminderState {
    DISABLED,
    PENDING,
    ACTIVE,
    EXPIRED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    modifier: Modifier = Modifier,
    initialReminder: Long? = null,
    onReminderChanged: (reminderTime: Long?, reminderText: String?) -> Unit
) {
    val context = LocalContext.current

    // Permission launcher for notifications (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, now check exact alarm permission
            checkExactAlarmPermission(context)
        } else {
            // Handle permission denied - you could show a message
            // that reminders might not work properly
        }
    }

    /**
     * Request permissions needed for reminders
     */
    fun requestReminderPermissions() {
        // For Android 13+ (API 33+), request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, check exact alarms
                    checkExactAlarmPermission(context)
                }
                else -> {
                    // Request notification permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older Android versions, just check exact alarms
            checkExactAlarmPermission(context)
        }
    }

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

    // Determine current reminder state
    val reminderState by remember(reminderConfig) {
        derivedStateOf {
            when {
                !reminderConfig.enabled -> ReminderState.DISABLED
                !reminderConfig.dateTime.isSet -> ReminderState.PENDING
                System.currentTimeMillis() > reminderConfig.dateTime.timestamp -> ReminderState.EXPIRED
                else -> ReminderState.ACTIVE
            }
        }
    }

    // Get colors based on reminder state
    val containerColor = when (reminderState) {
        ReminderState.DISABLED -> Color.Gray.copy(alpha = 0.03f)
        ReminderState.PENDING -> colorResource(id = R.color.nice_color).copy(alpha = 0.06f)
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color).copy(alpha = 0.06f)
        ReminderState.EXPIRED -> Color(0xFFFF8A50).copy(alpha = 0.08f) // Softer orange with slightly higher alpha
    }

    val borderColor = when (reminderState) {
        ReminderState.DISABLED -> null
        ReminderState.PENDING -> colorResource(id = R.color.nice_color).copy(alpha = 0.2f)
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color).copy(alpha = 0.2f)
        ReminderState.EXPIRED -> Color(0xFFFF8A50).copy(alpha = 0.25f) // Slightly more visible border
    }

    val contentColor = when (reminderState) {
        ReminderState.DISABLED -> Color.Gray
        ReminderState.PENDING -> colorResource(id = R.color.nice_color)
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color)
        ReminderState.EXPIRED -> Color(0xFFE65100) // Deeper, more sophisticated orange
    }

    val iconTint = when (reminderState) {
        ReminderState.DISABLED -> Color.Gray
        ReminderState.PENDING -> colorResource(id = R.color.nice_color)
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color)
        ReminderState.EXPIRED -> Color(0xFFFF7043) // Balanced orange between background and text
    }

    // Compact Main Reminder Card - reduced padding and size
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp), // Slightly smaller corner radius
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Column(modifier = Modifier.padding(14.dp)) { // Reduced from 18dp to 14dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (reminderState) {
                        ReminderState.EXPIRED -> Icons.Default.History
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = "Reminder",
                    tint = iconTint,
                    modifier = Modifier.size(18.dp) // Reduced from 20dp
                )

                Spacer(modifier = Modifier.width(10.dp)) // Reduced from 12dp

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (reminderState) {
                            ReminderState.EXPIRED -> "Reminder (Expired)"
                            else -> "Reminder"
                        },
                        fontSize = 15.sp, // Reduced from 16sp
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = when (reminderState) {
                            ReminderState.DISABLED -> "Get notified before deadline"
                            ReminderState.PENDING -> "Tap to set date and time"
                            ReminderState.ACTIVE -> "${reminderConfig.dateTime.date} at ${reminderConfig.dateTime.time}"
                            ReminderState.EXPIRED -> "${reminderConfig.dateTime.date} at ${reminderConfig.dateTime.time}"
                        },
                        fontSize = 12.sp, // Reduced from 13sp
                        color = when (reminderState) {
                            ReminderState.EXPIRED -> contentColor.copy(alpha = 0.7f)
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(top = 1.dp) // Reduced from 2dp
                    )
                }

                Switch(
                    checked = reminderConfig.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Request permissions when user enables reminders
                            requestReminderPermissions()
                            // Enable the reminder
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
                        checkedThumbColor = when (reminderState) {
                            ReminderState.EXPIRED -> Color(0xFFFF7043)
                            else -> colorResource(id = R.color.nice_color)
                        },
                        checkedTrackColor = when (reminderState) {
                            ReminderState.EXPIRED -> Color(0xFFFF7043).copy(alpha = 0.3f)
                            else -> colorResource(id = R.color.nice_color).copy(alpha = 0.3f)
                        },
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.scale(0.85f) // Slightly smaller switch
                )
            }

            // Compact reminder status - show different states
            when (reminderState) {
                ReminderState.ACTIVE -> {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                    modifier = Modifier.size(14.dp),
                                    tint = colorResource(id = R.color.nice_color)
                                )
                                Text(
                                    "Active • Tap to edit",
                                    fontSize = 12.sp,
                                    color = colorResource(id = R.color.nice_color)
                                )
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colorResource(id = R.color.nice_color).copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                ReminderState.EXPIRED -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = { showReminderDialog = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime, // Keep same icon for consistency
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFF7043)
                                )
                                Text(
                                    "Expired • Tap to update",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Medium // Add slight weight for better readability
                                )
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFF8A50).copy(alpha = 0.12f) // Slightly more prominent background
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                ReminderState.PENDING -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(id = R.color.nice_color)
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                ReminderState.DISABLED -> {
                    // No additional UI when disabled
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
                                imageVector = when (reminderState) {
                                    ReminderState.EXPIRED -> Icons.Default.AccessTime // Keep consistent icon
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = "Set Reminder",
                                tint = when (reminderState) {
                                    ReminderState.EXPIRED -> Color(0xFFFF7043)
                                    else -> colorResource(id = R.color.nice_color)
                                },
                                modifier = Modifier.size(20.dp) // Smaller icon
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (reminderState) {
                                    ReminderState.EXPIRED -> "Update Reminder"
                                    else -> "Set Reminder"
                                },
                                fontSize = 18.sp, // Reduced from 20sp
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }

                        // Show expired notice if applicable
                        if (reminderState == ReminderState.EXPIRED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF8A50).copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFFF7043)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "This reminder time has already passed",
                                        fontSize = 12.sp,
                                        color = Color(0xFFE65100),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
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



/**
 * Check and request exact alarm permission for Android 12+
 */
private fun checkExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            // Direct user to settings to enable exact alarms
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }
}