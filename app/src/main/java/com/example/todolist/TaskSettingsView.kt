@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todolist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.example.todolist.ui.theme.LocalDynamicColors
import com.example.todolist.notifications.canShowNotifications
import com.example.todolist.settings.HistoryCleanupWorker
import com.example.todolist.settings.SettingsViewModel
import java.util.concurrent.TimeUnit

// Helper function to check notification permission
private fun checkNotificationPermission(context: Context): Boolean {
    return canShowNotifications(context)
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect states from ViewModel
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val clearHistoryEnabled by viewModel.clearHistoryEnabled.collectAsState()

    var backPressed by remember { mutableStateOf(false)}
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Track notification permission status - this is the key fix
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    // Track if we should show rationale
    var shouldShowRationale by remember { mutableStateOf(false) }

    // Permission launcher for notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            // Permission granted - enable notifications
            viewModel.updateNotificationsEnabled(true)
        } else {
            // Permission denied - disable notifications and show rationale if needed
            viewModel.updateNotificationsEnabled(false)
            shouldShowRationale = true
            showPermissionDialog = true
        }
    }

    // Function to update permission state and sync with ViewModel
    fun updatePermissionStates() {
        val currentPermissionState = checkNotificationPermission(context)
        hasNotificationPermission = currentPermissionState

        // Sync app setting with actual permission state
        if (!currentPermissionState && notificationsEnabled) {
            // Permission was revoked externally, update our setting
            viewModel.updateNotificationsEnabled(false)
        } else if (currentPermissionState && !notificationsEnabled) {
            // Permission was granted externally but our setting is off
            // Don't auto-enable, let user choose
        }
    }

    // Check permission status when screen is resumed - CRITICAL FIX
    LifecycleResumeEffect(Unit) {
        updatePermissionStates()
        onPauseOrDispose { }
    }

    // Initial permission check
    LaunchedEffect(Unit) {
        updatePermissionStates()
    }

    // Handle history cleanup work scheduling
    LaunchedEffect(clearHistoryEnabled) {
        if (clearHistoryEnabled) {
            scheduleHistoryCleanup(context, true)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("history_cleanup_work")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (!backPressed) {
                            backPressed = true
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(id = R.color.dropMenuIcon_gray)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Theme & Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    // Theme Mode Setting
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = when (currentThemeMode) {
                            ThemeMode.LIGHT -> "Light theme"
                            ThemeMode.DARK -> "Dark theme"
                            ThemeMode.SYSTEM -> "Follow system theme"
                        },
                        onClick = { showThemeDialog = true }
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    // Accent Color Setting
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Accent Color",
                        subtitle = "Customize app colors",
                        onClick = { showColorPicker = true },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(accentColor)
                                )
                            }
                        }
                    )
                }
            }

            // Notifications & Reminders Section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Task Notifications",
                        subtitle = getNotificationSubtitle(
                            hasPermission = hasNotificationPermission,
                            isEnabled = notificationsEnabled
                        ),
                        // FIXED: Switch state now properly reflects both permission AND user preference
                        checked = notificationsEnabled && hasNotificationPermission,
                        enabled = true,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // User wants to enable notifications
                                if (hasNotificationPermission) {
                                    // Permission already granted, just enable
                                    viewModel.updateNotificationsEnabled(true)
                                } else {
                                    // Need to request permission
                                    requestNotificationPermission(
                                        context = context,
                                        launcher = notificationPermissionLauncher,
                                        onShowRationale = {
                                            shouldShowRationale = true
                                            showPermissionDialog = true
                                        }
                                    )
                                }
                            } else {
                                // User wants to disable notifications
                                viewModel.updateNotificationsEnabled(false)
                            }
                        }
                    )
                }
            }

            // Data & Privacy Section
            item {
                SettingsSection(title = "Data & Privacy") {
                    SettingsSwitchItem(
                        icon = Icons.Default.History,
                        title = "Auto-clear task history",
                        subtitle = if (clearHistoryEnabled) {
                            "All finished tasks will be cleared every 30 days"
                        } else {
                            "Finished tasks will be kept indefinitely"
                        },
                        checked = clearHistoryEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateClearHistoryEnabled(enabled)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentThemeMode,
            onThemeSelected = { theme ->
                viewModel.updateThemeMode(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = accentColor,
            onColorSelected = { color ->
                viewModel.updateAccentColor(color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Permission Dialog
    if (showPermissionDialog) {
        NotificationPermissionDialog(
            shouldShowRationale = shouldShowRationale,
            onDismiss = {
                showPermissionDialog = false
                shouldShowRationale = false
            },
            onOpenSettings = {
                showPermissionDialog = false
                shouldShowRationale = false
                openAppNotificationSettings(context)
            },
            onRetry = {
                showPermissionDialog = false
                shouldShowRationale = false
                requestNotificationPermission(
                    context = context,
                    launcher = notificationPermissionLauncher,
                    onShowRationale = {
                        shouldShowRationale = true
                        showPermissionDialog = true
                    }
                )
            }
        )
    }
}

/**
 * Get appropriate subtitle text for notification setting
 */
private fun getNotificationSubtitle(hasPermission: Boolean, isEnabled: Boolean): String {
    return when {
        !hasPermission -> "Permission required - tap to enable"
        isEnabled -> "Get reminded about your tasks"
        else -> "Tap to enable task notifications"
    }
}

/**
 * Request notification permission with proper handling
 */
private fun requestNotificationPermission(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    onShowRationale: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        // For older Android versions, notifications are enabled by default
        // but we should still check if they're disabled in system settings
        if (!checkNotificationPermission(context)) {
            onShowRationale()
        }
    }
}

/**
 * Open app notification settings
 */
private fun openAppNotificationSettings(context: Context) {
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
 * Improved notification permission dialog
 */
@Composable
private fun NotificationPermissionDialog(
    shouldShowRationale: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (shouldShowRationale) {
                    "Notification Permission Required"
                } else {
                    "Enable Notifications"
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Text(
                text = if (shouldShowRationale) {
                    "Notifications are disabled for this app. To receive task reminders, please enable notifications in your device settings."
                } else {
                    "Allow notifications to get reminded about your tasks and deadlines."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            if (shouldShowRationale) {
                TextButton(onClick = onOpenSettings) {
                    Text(
                        "Open Settings",
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                TextButton(onClick = onRetry) {
                    Text(
                        "Allow",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// Helper function to schedule history cleanup work
private fun scheduleHistoryCleanup(context: Context, enabled: Boolean) {
    val workManager = WorkManager.getInstance(context)

    if (enabled) {
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(30, TimeUnit.DAYS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "history_cleanup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            cleanupWorkRequest
        )
    } else {
        workManager.cancelUniqueWork("history_cleanup_work")
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailingContent?.invoke()
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                if (enabled) onCheckedChange(!checked)
            }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.1f else 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.6f)
            )
            subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.6f)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledCheckedThumbColor = MaterialTheme.colorScheme.outline,
                disabledCheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Choose theme",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentTheme == theme,
                                onClick = { onThemeSelected(theme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when (theme) {
                                ThemeMode.LIGHT -> "Light theme"
                                ThemeMode.DARK -> "Dark theme"
                                ThemeMode.SYSTEM -> "Follow system theme"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Done",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // Access dynamic colors
    val dynamicColors = LocalDynamicColors.current

    // Current selected color state, default to current color
    var selectedColor by remember { mutableStateOf(currentColor) }

    val predefinedColors = listOf(
        Color(0xFF0733F5), // Blue
        Color(0xFF7C3AED), // Rich Purple
        Color(0xFF059669), // Rich Emerald
        Color(0xFFEA580C), // Rich Orange
        Color(0xFFDC2626), // Rich Red
        Color(0xFF9333EA), // Rich Violet
        Color(0xFF1E40AF), // Deep Blue
        Color(0xFF0F766E), // Deep Teal
        Color(0xFFCA8A04), // Rich Amber
        Color(0xFF92400E), // Rich Brown
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp)
            ) {
                Text(
                    text = "Choose accent color",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Color Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    predefinedColors.chunked(5).forEach { rowColors ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowColors.forEach { color ->
                                ColorOption(
                                    color = color,
                                    isSelected = selectedColor == color,
                                    onClick = {
                                        selectedColor = color
                                        onColorSelected(color)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                CircleShape
            )
            .clickable { onClick() }
            .padding(if (isSelected) 0.dp else 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Selected state - full color background with white check
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Unselected state - color dot in center
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}