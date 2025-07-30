package com.example.todolist

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import com.example.todolist.data.Task
import com.example.todolist.notifications.AndroidReminderScheduler
import com.example.todolist.notifications.showNotification
import java.util.Date

data class ReminderConfig(
    val enabled: Boolean = false
)

@SuppressLint("ScheduleExactAlarm")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel,
    id: Long
) {
    var reminderConfig by remember { mutableStateOf(ReminderConfig()) }
    val context = LocalContext.current.applicationContext

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // You can update UI or state if needed after notification permission granted
    }

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
                        text = if (reminderConfig.enabled) "Reminder is ON" else "Get notified before deadline",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Switch(
                    checked = reminderConfig.enabled,
                    onCheckedChange = onCheckedChange@{ enabled ->
                        if (enabled) {
                            val task = Task(
                                id = 1L,
                                title = "Test Task",
                                description = "This is a description for the test task.",
                                date = Date().toString(),
                                address = "123 Main St",
                                priority = "High",
                                deadline = Date().toString(),
                                isDeleted = false,
                                deletionDate = null,
                                isCompleted = false,
                                completionDate = null,
                                label = "Work",
                                reminder = System.currentTimeMillis() + 60_000L
                            )

                            // Check SCHEDULE_EXACT_ALARM permission for Android S+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager = context.getSystemService(AlarmManager::class.java)
                                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                                    // Open system settings so user can grant permission manually
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    return@onCheckedChange
                                }
                            }
                            reminderConfig = reminderConfig.copy(enabled = enabled)

                            // Check POST_NOTIFICATIONS permission for Android TIRAMISU+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val permissionGranted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (permissionGranted) {
                                    val scheduler = AndroidReminderScheduler(context)
                                    scheduler.schedule(task)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                val scheduler = AndroidReminderScheduler(context)
                                scheduler.schedule(task)
                            }
                        }

                        reminderConfig = reminderConfig.copy(enabled = enabled)
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

            if (reminderConfig.enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(1) {
                        AssistChip(
                            onClick = { /* Future action */ },
                            label = { Text("Reminder active") }
                        )
                    }
                }
            }
        }
    }
}
