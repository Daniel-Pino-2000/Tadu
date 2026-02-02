package com.myapp.tadu

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.myapp.tadu.Graph.taskRepository
import com.myapp.tadu.navigation.Navigation
import com.myapp.tadu.ui.theme.MyToDoAppTheme
import com.myapp.tadu.settings.createSettingsRepository
import com.myapp.tadu.settings.SettingsViewModel
import com.myapp.tadu.view_model.AuthViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // State for showing battery optimization dialog
    private var showBatteryOptimizationDialog by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Create settings repository and view model
            val settingsRepository = remember {
                this@MainActivity.createSettingsRepository()
            }
            val settingsViewModel = remember { SettingsViewModel(settingsRepository) }

            // Collect the combined settings state
            val settingsState by settingsViewModel.settingsState.collectAsState()



            // Simplified loading state - single state controls everything
            var showMainContent by remember { mutableStateOf(false) }

            // Determine theme - handle this in Composable context
            val systemInDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = when (settingsState.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }

            // Streamlined loading sequence
            LaunchedEffect(settingsState.settingsLoaded) {
                if (settingsState.settingsLoaded) {
                    delay(500) // Shorter delay for snappier feel
                    showMainContent = true
                }
            }

            // Apply theme consistently
            MyToDoAppTheme(
                darkTheme = isDarkTheme,
                accentColor = settingsState.accentColor
            ) {
                SetSystemBarsColor(color = MaterialTheme.colorScheme.background)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {
                        // Loading screen - only shows when main content is hidden
                        if (!showMainContent) {
                            LoadingScreen()
                        }

                        // Main content with smooth fade-in, no overlapping animations
                        AnimatedVisibility(
                            visible = showMainContent,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            MainContent(settingsViewModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        // Single smooth rotation animation - no complex overlapping animations
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200, // Slightly slower for smoother feel
                    easing = LinearEasing
                )
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun MainContent(settingsViewModel: SettingsViewModel) {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()
        val settingsState by settingsViewModel.settingsState.collectAsState()

        // Check if user is logged in
        var isLoggedIn by remember {
            mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
        }

        DisposableEffect(Unit) {
            val auth = FirebaseAuth.getInstance()
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                isLoggedIn = firebaseAuth.currentUser != null
            }

            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        }

        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn) {
                navController.navigate("login") {
                    popUpTo(0)
                }
            } else {
                taskRepository.syncFromCloud()
            }
        }


        // Check battery optimization on startup (only if notifications are enabled)
        LaunchedEffect(settingsState.notificationsEnabled) {
            if (settingsState.notificationsEnabled) {
                delay(1500) // Slightly shorter delay
                if (!isBatteryOptimizationDisabled()) {
                    showBatteryOptimizationDialog = true
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Navigation(navController = navController, authViewModel = authViewModel)

            // Show battery optimization dialog when needed
            if (showBatteryOptimizationDialog) {
                PermissionExplanationDialog(
                    onConfirm = {
                        showBatteryOptimizationDialog = false
                        openBatteryOptimizationSettings()
                    },
                    onDismiss = { showBatteryOptimizationDialog = false }
                )
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
                    data = Uri.parse("package:$packageName")
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
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
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