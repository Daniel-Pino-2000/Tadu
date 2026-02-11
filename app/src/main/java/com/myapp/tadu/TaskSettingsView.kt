@file:OptIn(ExperimentalMaterial3Api::class)

package com.myapp.tadu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.myapp.tadu.ui.theme.LocalDynamicColors
import com.myapp.tadu.ui.theme.getCommonAccentColors
import com.myapp.tadu.notifications.canShowNotifications
import com.myapp.tadu.settings.SettingsViewModel
import com.myapp.tadu.view_model.AuthViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// Helper function to check notification permission
private fun checkNotificationPermission(context: Context): Boolean {
    return canShowNotifications(context)
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val dynamicColors = LocalDynamicColors.current

    // Collect states from ViewModel
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    // Delete account states
    val accountDeleted by authViewModel.accountDeleted.observeAsState(false)
    val deleteAccountError by authViewModel.deleteAccountError.observeAsState()
    val deleteAccountLoading by authViewModel.deleteAccountLoading.observeAsState(false)

    // Current user data
    val currentUser by authViewModel.currentUser.observeAsState()
    val userEmail = authViewModel.getCurrentUserEmail() ?: "No email"

    // Track previous value of clearHistoryEnabled to detect actual changes
    var previousClearHistoryEnabled by remember { mutableStateOf<Boolean?>(null) }

    // Determine current dark theme state in Composable context
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (currentThemeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }

    var backPressed by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Track notification permission status
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }
    var shouldShowRationale by remember { mutableStateOf(false) }

    // Permission launcher for notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.updateNotificationsEnabled(true)
        } else {
            viewModel.updateNotificationsEnabled(false)
            shouldShowRationale = true
            showPermissionDialog = true
        }
    }

    // Function to update permission state and sync with ViewModel
    fun updatePermissionStates() {
        val currentPermissionState = checkNotificationPermission(context)
        hasNotificationPermission = currentPermissionState

        if (!currentPermissionState && notificationsEnabled) {
            viewModel.updateNotificationsEnabled(false)
        }
    }

    // Check permission status when screen is resumed
    LifecycleResumeEffect(Unit) {
        updatePermissionStates()
        onPauseOrDispose { }
    }

    // Initial permission check
    LaunchedEffect(Unit) {
        updatePermissionStates()
        // Load current user data
        authViewModel.loadCurrentUser()
    }

    // Handle system theme changes for accent color updates
    LaunchedEffect(systemInDarkTheme, currentThemeMode, accentColor) {
        // Only update accent color if we're following system theme and the effective theme changed
        if (currentThemeMode == ThemeMode.SYSTEM) {
            val oldColors = getCommonAccentColors(!systemInDarkTheme) // Previous theme colors
            val newColors = getCommonAccentColors(systemInDarkTheme)   // Current theme colors

            // Find current accent color index in the old theme set
            val currentIndex = oldColors.indexOfFirst { color ->
                abs(color.red - accentColor.red) < 0.01f &&
                        abs(color.green - accentColor.green) < 0.01f &&
                        abs(color.blue - accentColor.blue) < 0.01f
            }

            // If we found a match and the colors are different, update to the corresponding color
            if (currentIndex >= 0 && currentIndex < newColors.size) {
                val newAccentColor = newColors[currentIndex]
                if (newAccentColor != accentColor) {
                    viewModel.updateAccentColor(newAccentColor)
                }
            }
        }
    }

    // Observe accountDeleted LiveData - using DisposableEffect for immediate response
    DisposableEffect(accountDeleted) {
        if (accountDeleted) {
            // Clear the flag immediately
            authViewModel.clearAccountDeleted()

            // Navigate to login with complete stack clear
            try {
                navController.navigate("login") {
                    // Clear all back stack
                    popUpTo(0) { inclusive = true }
                    // Prevent multiple instances
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                // If navigation fails, we're already on login or there's an issue
                android.util.Log.e("SettingsScreen", "Navigation error after account deletion", e)
            }
        }

        onDispose { }
    }

    // Handle delete account errors
    LaunchedEffect(deleteAccountError) {
        deleteAccountError?.let {
            // Error is shown in the dialog
            kotlinx.coroutines.delay(3000) // Show error for 3 seconds
            authViewModel.clearDeleteAccountError()
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
                        tint = dynamicColors.dropMenuIconGray
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

            // User Profile Section
            item {
                UserProfileSection(
                    userName = currentUser?.let { "${it.firstName} ${it.lastName}" },
                    userEmail = userEmail
                )
            }

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

                    HorizontalDivider(
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
                                    .background(dynamicColors.niceColor)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(dynamicColors.niceColor)
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
                        checked = notificationsEnabled && hasNotificationPermission,
                        enabled = true,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (hasNotificationPermission) {
                                    viewModel.updateNotificationsEnabled(true)
                                } else {
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
                                viewModel.updateNotificationsEnabled(false)
                            }
                        }
                    )
                }
            }

            // Account Section
            item {
                SettingsSection(title = "Account") {
                    SettingsItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            // Danger Zone Section
            item {
                SettingsSection(title = "Danger zone") {
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Delete account",
                        subtitle = "Permanently delete your account and all data",
                        titleColor = MaterialTheme.colorScheme.error,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? You'll need to sign in again to access your tasks.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Logout",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Delete Account Dialog with Password Confirmation
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            isLoading = deleteAccountLoading,
            error = deleteAccountError,
            onConfirm = { password ->
                authViewModel.deleteUserAccount(password)
            },
            onDismiss = {
                if (!deleteAccountLoading) {
                    showDeleteAccountDialog = false
                    authViewModel.clearDeleteAccountError()
                }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentThemeMode,
            onThemeSelected = { theme ->
                // Determine current effective theme
                val currentIsDarkTheme = when (currentThemeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> systemInDarkTheme
                }

                // Determine new effective theme
                val newIsDarkTheme = when (theme) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> systemInDarkTheme
                }

                // Update theme mode first
                viewModel.updateThemeMode(theme)

                // Only update accent color if effective theme changes
                if (newIsDarkTheme != currentIsDarkTheme) {
                    // Find the closest matching color from the new theme's color set
                    val oldColors = getCommonAccentColors(currentIsDarkTheme) // Old theme colors
                    val newColors = getCommonAccentColors(newIsDarkTheme)     // New theme colors

                    // Find the index of the current color in the old theme's colors
                    val currentIndex = oldColors.indexOfFirst { color ->
                        abs(color.red - accentColor.red) < 0.01f &&
                                abs(color.green - accentColor.green) < 0.01f &&
                                abs(color.blue - accentColor.blue) < 0.01f
                    }

                    // If we found a match, use the same index in the new theme colors
                    val newAccentColor = if (currentIndex >= 0 && currentIndex < newColors.size) {
                        newColors[currentIndex]
                    } else {
                        newColors[0] // Default to first color if no match found
                    }

                    // Update the accent color to the theme-appropriate version
                    viewModel.updateAccentColor(newAccentColor)
                }

                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = accentColor,
            isDarkTheme = isDarkTheme,
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
 * Delete Account Dialog with Password Confirmation
 */
@Composable
private fun DeleteAccountDialog(
    isLoading: Boolean,
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                "Delete account",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "This will permanently delete your account and all your tasks. " +
                            "This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter your password") },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.Visibility
                                } else {
                                    Icons.Default.VisibilityOff
                                },
                                contentDescription = if (passwordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                }
                            )
                        }
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (password.isNotBlank()) {
                                onConfirm(password)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.error
                    )
                )

                // Show error if there is one
                error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                ),
                onClick = {
                    if (password.isNotBlank()) {
                        focusManager.clearFocus()
                        onConfirm(password)
                    }
                },
                enabled = !isLoading && password.isNotBlank()
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Deleting...",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        "Delete permanently",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * User Profile Section - Shows user info at the top of settings
 */
@Composable
private fun UserProfileSection(
    userName: String?,
    userEmail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                // Show first letter of name or default icon
                if (userName != null && userName.isNotBlank()) {
                    Text(
                        text = userName.first().uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User profile",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // User Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // User Name
                if (userName != null && userName.isNotBlank()) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // User Email
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
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
    launcher: ActivityResultLauncher<String>,
    onShowRationale: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
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
                data = Uri.fromParts("package", context.packageName, null)
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
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
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
                tint = iconTint,
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
                color = titleColor
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
    isDarkTheme: Boolean,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(currentColor) }

    // Use theme-specific accent colors for optimal visibility
    val predefinedColors = getCommonAccentColors(isDarkTheme)

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

                // Color Grid - shows 2 rows with 4 colors each
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    predefinedColors.chunked(4).forEach { rowColors ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                if (isSelected) {
                    color
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
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