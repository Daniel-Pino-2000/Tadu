package com.example.todolist

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.todolist.notifications.canShowNotifications
import kotlinx.coroutines.flow.StateFlow

enum class ReminderState {
    DISABLED,           // User has disabled reminders
    PENDING,           // Enabled but no time set
    ACTIVE,            // Enabled with valid future time
    EXPIRED,           // Enabled with past time
    PERMISSION_DENIED  // Permissions missing - can still interact
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    themeMode: StateFlow<ThemeMode>?,
    modifier: Modifier = Modifier,
    initialReminder: Long? = null,
    onReminderChanged: (Long?, String?) -> Unit,
) {
    val context = LocalContext.current

    val themeModeValue: ThemeMode? = themeMode?.collectAsState()?.value

    val isDarkTheme: Boolean = when (themeModeValue) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        null -> isSystemInDarkTheme() // fallback when no VM passed
    }




    // Reminder configuration state
    var reminderConfig by remember {
        mutableStateOf(
            ReminderConfig(
                enabled = initialReminder != null,
                dateTime = initialReminder?.toReminderDateTime() ?: ReminderDateTime()
            )
        )
    }

    // Permission states - tracked separately from reminder config
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var hasExactAlarmPermission by remember { mutableStateOf(false) }
    var shouldShowPermissionDialog by remember { mutableStateOf(false) }
    var permissionDialogType by remember { mutableStateOf(PermissionDialogType.NOTIFICATION) }

    // Dialog states
    var showReminderDialog by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            // Permission denied - show dialog to explain and offer settings
            permissionDialogType = PermissionDialogType.NOTIFICATION
            shouldShowPermissionDialog = true
        } else {
            // Notification permission granted, check exact alarm permission
            checkAndRequestExactAlarmPermission(context) { hasExactAlarm ->
                hasExactAlarmPermission = hasExactAlarm
                if (!hasExactAlarm) {
                    permissionDialogType = PermissionDialogType.EXACT_ALARM
                    shouldShowPermissionDialog = true
                } else {
                    // Both permissions granted, proceed to set reminder
                    showReminderDialog = true
                }
            }
        }
    }



    // Check permissions on composition and when returning from settings
    fun updatePermissionStates() {
        hasNotificationPermission = canShowNotifications(context)
        hasExactAlarmPermission = canScheduleExactAlarms(context)
    }

    // Initial permission check and when screen resumes
    LaunchedEffect(Unit) {
        updatePermissionStates()
    }

    // Update permissions when returning from system settings
    LifecycleResumeEffect(Unit) {
        updatePermissionStates()
        onPauseOrDispose { }
    }

    // Update reminderConfig when initialReminder changes
    LaunchedEffect(initialReminder) {
        reminderConfig = ReminderConfig(
            enabled = initialReminder != null,
            dateTime = initialReminder?.toReminderDateTime() ?: ReminderDateTime()
        )
    }

    // Determine current reminder state
    val reminderState by remember(reminderConfig, hasNotificationPermission, hasExactAlarmPermission) {
        derivedStateOf {
            when {
                !reminderConfig.enabled -> ReminderState.DISABLED
                !reminderConfig.dateTime.isSet -> {
                    if (hasNotificationPermission && hasExactAlarmPermission) {
                        ReminderState.PENDING
                    } else {
                        ReminderState.PERMISSION_DENIED
                    }
                }
                System.currentTimeMillis() > reminderConfig.dateTime.timestamp -> ReminderState.EXPIRED
                !hasNotificationPermission || !hasExactAlarmPermission -> ReminderState.PERMISSION_DENIED
                else -> ReminderState.ACTIVE
            }
        }
    }

    // Handle reminder toggle - this should ALWAYS be allowed
    fun handleReminderToggle(enabled: Boolean) {
        if (enabled) {
            // User wants to enable reminders
            reminderConfig = reminderConfig.copy(enabled = true)

            // Check permissions and guide user accordingly
            when {
                !hasNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // For older versions, show settings dialog
                        permissionDialogType = PermissionDialogType.NOTIFICATION
                        shouldShowPermissionDialog = true
                    }
                }
                !hasExactAlarmPermission -> {
                    permissionDialogType = PermissionDialogType.EXACT_ALARM
                    shouldShowPermissionDialog = true
                }
                else -> {
                    // All permissions available, show date picker
                    if (!reminderConfig.dateTime.isSet) {
                        showReminderDialog = true
                    }
                }
            }
        } else {
            // User wants to disable reminders - always allowed
            reminderConfig = reminderConfig.copy(
                enabled = false,
                dateTime = ReminderDateTime()
            )
            onReminderChanged(null, null)
        }
    }

    // Theme-aware colors based on reminder state
    val containerColor = when (reminderState) {
        ReminderState.DISABLED -> if (isDarkTheme) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        } else {
            Color.Gray.copy(alpha = 0.03f)
        }
        ReminderState.PERMISSION_DENIED -> Color(0xFFFFA726).copy(
            alpha = if (isDarkTheme) 0.15f else 0.08f
        )
        ReminderState.PENDING -> colorResource(id = R.color.nice_color).copy(
            alpha = if (isDarkTheme) 0.12f else 0.06f
        )
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color).copy(
            alpha = if (isDarkTheme) 0.12f else 0.06f
        )
        ReminderState.EXPIRED -> Color(0xFFFF8A50).copy(
            alpha = if (isDarkTheme) 0.15f else 0.08f
        )
    }

    val borderColor = when (reminderState) {
        ReminderState.DISABLED -> if (isDarkTheme) {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        } else null
        ReminderState.PERMISSION_DENIED -> Color(0xFFFFA726).copy(
            alpha = if (isDarkTheme) 0.4f else 0.3f
        )
        ReminderState.PENDING -> colorResource(id = R.color.nice_color).copy(
            alpha = if (isDarkTheme) 0.4f else 0.2f
        )
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color).copy(
            alpha = if (isDarkTheme) 0.4f else 0.2f
        )
        ReminderState.EXPIRED -> Color(0xFFFF8A50).copy(
            alpha = if (isDarkTheme) 0.5f else 0.25f
        )
    }

    val contentColor = when (reminderState) {
        ReminderState.DISABLED -> if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        } else Color.Gray
        ReminderState.PERMISSION_DENIED -> Color(0xFFFFA726)
        ReminderState.PENDING -> colorResource(id = R.color.nice_color)
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color)
        ReminderState.EXPIRED -> if (isDarkTheme) Color(0xFFFFAB91) else Color(0xFFE65100)
    }

    val iconTint = when (reminderState) {
        ReminderState.DISABLED -> if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        } else Color.Gray
        ReminderState.PERMISSION_DENIED -> Color(0xFFFFA726)
        ReminderState.PENDING -> colorResource(id = R.color.nice_color)
        ReminderState.ACTIVE -> colorResource(id = R.color.nice_color)
        ReminderState.EXPIRED -> if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFFF7043)
    }



    // Main Reminder Card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 2.dp else 0.dp),
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (reminderState) {
                        ReminderState.EXPIRED -> Icons.Default.History
                        ReminderState.PERMISSION_DENIED -> Icons.Default.Settings
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = "Reminder",
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (reminderState) {
                            ReminderState.EXPIRED -> "Reminder (Expired)"
                            ReminderState.PERMISSION_DENIED -> "Reminder (Permissions Needed)"
                            else -> "Reminder"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = getReminderSubtitle(reminderState, reminderConfig, hasNotificationPermission, hasExactAlarmPermission),
                        fontSize = 12.sp,
                        color = when (reminderState) {
                            ReminderState.EXPIRED, ReminderState.PERMISSION_DENIED -> contentColor.copy(alpha = 0.7f)
                            else -> if (isDarkTheme) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else Color.Gray
                        },
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }

                // Switch is ALWAYS interactive - this is key for best practices
                Switch(
                    checked = reminderConfig.enabled,
                    onCheckedChange = { enabled -> handleReminderToggle(enabled) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = when (reminderState) {
                            ReminderState.EXPIRED -> if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFFF7043)
                            ReminderState.PERMISSION_DENIED -> Color(0xFFFFA726)
                            else -> colorResource(id = R.color.nice_color)
                        },
                        checkedTrackColor = when (reminderState) {
                            ReminderState.EXPIRED -> if (isDarkTheme) Color(0xFFFF8A65).copy(alpha = 0.4f) else Color(0xFFFF7043).copy(alpha = 0.3f)
                            ReminderState.PERMISSION_DENIED -> Color(0xFFFFA726).copy(alpha = 0.3f)
                            else -> colorResource(id = R.color.nice_color).copy(alpha = 0.3f)
                        },
                        uncheckedThumbColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface else Color.White,
                        uncheckedTrackColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }

            // Additional actions based on state
            when (reminderState) {
                ReminderState.PERMISSION_DENIED -> {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Show which permissions are missing
                    val missingPermissions = buildList {
                        if (!hasNotificationPermission) add("Notifications")
                        if (!hasExactAlarmPermission) add("Exact Alarms")
                    }

                    AssistChip(
                        onClick = {
                            permissionDialogType = when {
                                !hasNotificationPermission -> PermissionDialogType.NOTIFICATION
                                !hasExactAlarmPermission -> PermissionDialogType.EXACT_ALARM
                                else -> PermissionDialogType.NOTIFICATION
                            }
                            shouldShowPermissionDialog = true
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFA726)
                                )
                                Text(
                                    "Grant ${missingPermissions.joinToString(" & ")} Permission${if (missingPermissions.size > 1) "s" else ""}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFA726),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFFA726).copy(alpha = if (isDarkTheme) 0.2f else 0.1f)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

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
                            containerColor = colorResource(id = R.color.nice_color).copy(alpha = if (isDarkTheme) 0.2f else 0.1f)
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
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFFF7043)
                                )
                                Text(
                                    "Expired • Tap to update",
                                    fontSize = 12.sp,
                                    color = if (isDarkTheme) Color(0xFFFFAB91) else Color(0xFFE65100),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFF8A50).copy(alpha = if (isDarkTheme) 0.2f else 0.12f)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                ReminderState.PENDING -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
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

    // Reminder Date/Time Dialog - only show if permissions are available
    if (showReminderDialog && hasNotificationPermission && hasExactAlarmPermission) {
        Dialog(onDismissRequest = { showReminderDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Dialog Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (reminderState) {
                                    ReminderState.EXPIRED -> Icons.Default.AccessTime
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = "Set Reminder",
                                tint = when (reminderState) {
                                    ReminderState.EXPIRED -> if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFFF7043)
                                    else -> colorResource(id = R.color.nice_color)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (reminderState) {
                                    ReminderState.EXPIRED -> "Update Reminder"
                                    else -> "Set Reminder"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (reminderState == ReminderState.EXPIRED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF8A50).copy(alpha = if (isDarkTheme) 0.15f else 0.08f)
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
                                        tint = if (isDarkTheme) Color(0xFFFF8A65) else Color(0xFFFF7043)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "This reminder time has already passed",
                                        fontSize = 12.sp,
                                        color = if (isDarkTheme) Color(0xFFFFAB91) else Color(0xFFE65100),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date/Time Picker (you'll need to implement this)
                        ReminderDateTimePicker(
                            initialDateTime = reminderConfig.dateTime,
                            onReminderSet = { dateTime ->
                                reminderConfig = reminderConfig.copy(
                                    enabled = true,
                                    dateTime = dateTime
                                )
                                val reminderText = "Set for ${dateTime.date} at ${dateTime.time}"
                                onReminderChanged(dateTime.timestamp, reminderText)
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            TextButton(onClick = { showReminderDialog = false }) {
                                Text(
                                    text = "Cancel",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                                        onReminderChanged(null, null)
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
                }
            }
        }
    }

    // Permission Dialog
    if (shouldShowPermissionDialog) {
        PermissionDialog(
            permissionType = permissionDialogType,
            onDismiss = { shouldShowPermissionDialog = false },
            onOpenSettings = {
                shouldShowPermissionDialog = false
                when (permissionDialogType) {
                    PermissionDialogType.NOTIFICATION -> openNotificationSettings(context)
                    PermissionDialogType.EXACT_ALARM -> openExactAlarmSettings(context)
                }
            },
            onRetry = {
                shouldShowPermissionDialog = false
                when (permissionDialogType) {
                    PermissionDialogType.NOTIFICATION -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openNotificationSettings(context)
                        }
                    }
                    PermissionDialogType.EXACT_ALARM -> openExactAlarmSettings(context)
                }
            }
        )
    }
}

/**
 * Get subtitle text based on reminder state and permissions
 */
private fun getReminderSubtitle(
    state: ReminderState,
    config: ReminderConfig,
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean
): String {
    return when (state) {
        ReminderState.DISABLED -> "Get notified before deadline"
        ReminderState.PERMISSION_DENIED -> {
            val missing = buildList {
                if (!hasNotificationPermission) add("notification")
                if (!hasExactAlarmPermission) add("exact alarm")
            }
            "Missing ${missing.joinToString(" and ")} permission${if (missing.size > 1) "s" else ""}"
        }
        ReminderState.PENDING -> "Tap to set date and time"
        ReminderState.ACTIVE -> "${config.dateTime.date} at ${config.dateTime.time}"
        ReminderState.EXPIRED -> "${config.dateTime.date} at ${config.dateTime.time}"
    }
}

/**
 * Check if exact alarms can be scheduled
 */
private fun canScheduleExactAlarms(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

/**
 * Check and request exact alarm permission
 */
private fun checkAndRequestExactAlarmPermission(context: Context, onResult: (Boolean) -> Unit) {
    val hasPermission = canScheduleExactAlarms(context)
    onResult(hasPermission)
}

/**
 * Open notification settings
 */
private fun openNotificationSettings(context: Context) {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = android.net.Uri.fromParts("package", context.packageName, null)
            }
        }
    }
    context.startActivity(intent)
}

/**
 * Open exact alarm settings
 */
private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }
}

/**
 * Permission dialog types
 */
enum class PermissionDialogType {
    NOTIFICATION,
    EXACT_ALARM
}

/**
 * Permission dialog component
 */
@Composable
private fun PermissionDialog(
    permissionType: PermissionDialogType,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit
) {
    val (title, message, actionText) = when (permissionType) {
        PermissionDialogType.NOTIFICATION -> Triple(
            "Notification Permission",
            "To receive task reminders, this app needs notification permission. You can enable it in your device settings.",
            "Open Settings"
        )
        PermissionDialogType.EXACT_ALARM -> Triple(
            "Exact Alarm Permission",
            "For precise reminders, this app needs \"Alarms & reminders\" permission. Enable it in device settings to get timely notifications.",
            "Open Settings"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (permissionType) {
                    PermissionDialogType.NOTIFICATION -> Icons.Default.Notifications
                    PermissionDialogType.EXACT_ALARM -> Icons.Default.AccessTime
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(actionText, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
