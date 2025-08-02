package com.example.todolist

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.mytodoapp.ui.theme.MyToDoAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // State for showing battery optimization dialog
    private var showBatteryOptimizationDialog by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyToDoAppTheme {
                SetSystemBarsColor(color = MaterialTheme.colorScheme.background)

                val navController = rememberNavController()

                // Check battery optimization on startup
                LaunchedEffect(Unit) {
                    delay(2000) // Give user time to see the app first
                    if (!isBatteryOptimizationDisabled()) {
                        showBatteryOptimizationDialog = true
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {
                        Navigation(navController = navController)

                        // Show battery optimization dialog when needed
                        if (showBatteryOptimizationDialog) {
                            PermissionExplanationDialog (
                                onConfirm = {
                                    showBatteryOptimizationDialog = false
                                    openBatteryOptimizationSettings()
                                },
                                onDismiss = { showBatteryOptimizationDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Request battery optimization exemption - shows explanation dialog
     */
    fun requestBatteryOptimizationExemption() {
        showBatteryOptimizationDialog = true
    }

    /**
     * Open battery optimization settings
     */
    private fun openBatteryOptimizationSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback to battery optimization settings
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                // Final fallback to general settings
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
        }
    }

    /**
     * Check if battery optimization is disabled for this app
     */
    fun isBatteryOptimizationDisabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true // Not applicable for older versions
        }
    }

    override fun onResume() {
        super.onResume()

        // Check permissions when returning to app (user might have changed settings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // Permissions might have been revoked
            }
        }
    }
}

@Composable
fun PermissionExplanationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Title
                Text(
                    text = "Enable Reminders",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Description
                Text(
                    text = "To receive task reminders, please allow notifications in your device settings.\n\n" +
                            "This helps you:\n" +
                            "• Never miss important tasks\n" +
                            "• Stay organized and productive\n" +
                            "• Get reminded at the perfect time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Not Now")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

@Composable
fun SetSystemBarsColor(color: Color) {
    val view = LocalView.current
    val window = (view.context as ComponentActivity).window
    val useDarkIcons = color.luminance() > 0.5f

    SideEffect {
        val colorArgb = color.toArgb()
        window.statusBarColor = colorArgb
        window.navigationBarColor = colorArgb

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = useDarkIcons
            isAppearanceLightNavigationBars = useDarkIcons
        }
    }
}