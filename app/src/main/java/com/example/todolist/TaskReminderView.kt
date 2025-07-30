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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.platform.LocalContext

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

    Column(modifier = modifier) {
        // Main Reminder Toggle Card
        Card(
            modifier = Modifier
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
                                    "Choose date and time below"
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
                            }

                            reminderConfig = reminderConfig.copy(enabled = enabled)

                            if (!enabled) {
                                // Clear reminder when disabled
                                reminderConfig = reminderConfig.copy(dateTime = ReminderDateTime())
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

                // Show active reminder chip when enabled and set
                if (reminderConfig.enabled && reminderConfig.dateTime.isSet) {
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(1) {
                            AssistChip(
                                onClick = {
                                    // Could be used for additional options
                                },
                                label = { Text("Reminder active") }
                            )
                        }
                    }
                }
            }
        }

        // Animated Date/Time Picker
        AnimatedVisibility(
            visible = reminderConfig.enabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ReminderDateTimePicker(
                initialDateTime = reminderConfig.dateTime,
                onReminderSet = { dateTime ->
                    reminderConfig = reminderConfig.copy(dateTime = dateTime)

                    // Return the timestamp and display text
                    val reminderText = "Set for ${dateTime.date} at ${dateTime.time}"
                    onReminderChanged(dateTime.timestamp, reminderText)
                },
                onReminderCleared = {
                    reminderConfig = reminderConfig.copy(dateTime = ReminderDateTime())
                    onReminderChanged(null, null)
                }
            )
        }
    }
}