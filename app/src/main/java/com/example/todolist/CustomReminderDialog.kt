package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit,
    initialDateTime: LocalDateTime? = null,
    taskDeadline: LocalDateTime? = null // Add task deadline for validation
) {
    var selectedDate by remember {
        mutableStateOf(initialDateTime?.toLocalDate() ?: java.time.LocalDate.now())
    }
    var selectedTime by remember {
        mutableStateOf(initialDateTime?.toLocalTime() ?: java.time.LocalTime.of(9, 0))
    }
    var currentStep by remember { mutableStateOf(0) } // 0 = date, 1 = time
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentStep == 0) "Select Date" else "Select Time",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.nice_blue)
                        )
                        if (showError) {
                            Text(
                                text = "Reminder time must be before deadline",
                                fontSize = 10.sp,
                                color = Color.Red
                            )
                        }
                    }

                    // Step indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date step indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (currentStep == 0)
                                        colorResource(id = R.color.nice_blue)
                                    else
                                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )

                        // Connection line
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(
                                    color = colorResource(id = R.color.nice_blue).copy(alpha = 0.3f)
                                )
                        )

                        // Time step indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (currentStep == 1)
                                        colorResource(id = R.color.nice_blue)
                                    else
                                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary card showing current selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Reminder set for:",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.nice_blue)
                        )
                        Text(
                            text = selectedTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.nice_blue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on current step
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Fixed height to prevent layout shifts
                ) {
                    if (currentStep == 0) {
                        // Date Picker
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        )

                        // Update selectedDate when date picker changes
                        LaunchedEffect(datePickerState.selectedDateMillis) {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        }

                        Column {
                            DatePicker(
                                state = datePickerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp), // Add bottom padding to prevent overlap
                                showModeToggle = false,
                                colors = DatePickerDefaults.colors(
                                    selectedDayContainerColor = colorResource(id = R.color.nice_blue),
                                    todayDateBorderColor = colorResource(id = R.color.nice_blue)
                                )
                            )
                        }
                    } else {
                        // Time Picker
                        val timePickerState = rememberTimePickerState(
                            initialHour = selectedTime.hour,
                            initialMinute = selectedTime.minute,
                            is24Hour = false
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TimePickerDefaults.colors(
                                    selectorColor = colorResource(id = R.color.nice_blue),
                                    containerColor = Color.White,
                                    periodSelectorSelectedContainerColor = colorResource(id = R.color.nice_blue),
                                    periodSelectorUnselectedContainerColor = Color.Gray.copy(alpha = 0.1f),
                                    periodSelectorSelectedContentColor = Color.White,
                                    timeSelectorSelectedContainerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.2f),
                                    timeSelectorSelectedContentColor = colorResource(id = R.color.nice_blue)
                                )
                            )

                            // Update button to apply time changes
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    selectedTime = java.time.LocalTime.of(
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Update Time",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorResource(id = R.color.nice_blue)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Next/Confirm button
                    Button(
                        onClick = {
                            if (currentStep == 0) {
                                currentStep = 1
                                showError = false
                            } else {
                                val selectedDateTime = selectedDate.atTime(selectedTime)

                                // Validate against task deadline
                                if (taskDeadline != null && selectedDateTime.isAfter(taskDeadline)) {
                                    showError = true
                                    return@Button
                                }

                                onConfirm(selectedDateTime)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.nice_blue)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentStep == 0) "Next →" else "Set Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Back button on time step
                if (currentStep == 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                currentStep = 0
                                showError = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorResource(id = R.color.nice_blue)
                            )
                        ) {
                            Text(
                                text = "← Back to Date",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}