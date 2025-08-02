package com.example.todolist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDateTimePicker(
    modifier: Modifier = Modifier,
    onReminderSet: (ReminderDateTime) -> Unit,
    onReminderCleared: () -> Unit,
    initialDateTime: ReminderDateTime? = null
) {
    var reminderDateTime by remember {
        mutableStateOf(initialDateTime ?: ReminderDateTime())
    }
    val context = LocalContext.current

    // Date and time formatters
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance()

    // Initialize calendar with current reminder or current time
    if (reminderDateTime.timestamp > 0) {
        calendar.timeInMillis = reminderDateTime.timestamp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminderDateTime.isSet) {
                colorResource(id = R.color.nice_blue).copy(alpha = 0.06f)
            } else {
                Color.Gray.copy(alpha = 0.03f)
            }
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (reminderDateTime.isSet) {
            BorderStroke(
                1.dp,
                colorResource(id = R.color.nice_blue).copy(alpha = 0.2f)
            )
        } else null
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set Reminder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (reminderDateTime.isSet)
                        colorResource(id = R.color.nice_blue)
                    else Color.Black,
                    modifier = Modifier.weight(1f)
                )

                if (reminderDateTime.isSet) {
                    TextButton(
                        onClick = {
                            reminderDateTime = ReminderDateTime()
                            onReminderCleared()
                        }
                    ) {
                        Text(
                            text = "Clear",
                            color = colorResource(id = R.color.nice_blue),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Button
            OutlinedCard(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                            val selectedDate = dateFormat.format(calendar.time)
                            reminderDateTime = reminderDateTime.copy(
                                date = selectedDate,
                                timestamp = calendar.timeInMillis
                            )

                            // If time is also set, trigger the callback
                            if (reminderDateTime.time.isNotEmpty()) {
                                val updatedDateTime = reminderDateTime.copy(isSet = true)
                                reminderDateTime = updatedDateTime
                                onReminderSet(updatedDateTime)
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        // Set minimum date to today
                        datePicker.minDate = System.currentTimeMillis()
                    }.show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (reminderDateTime.date.isNotEmpty())
                        colorResource(id = R.color.nice_blue).copy(alpha = 0.05f)
                    else Color.Transparent
                ),
                border = BorderStroke(
                    1.dp,
                    if (reminderDateTime.date.isNotEmpty())
                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f)
                    else Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = if (reminderDateTime.date.isNotEmpty())
                            colorResource(id = R.color.nice_blue)
                        else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (reminderDateTime.date.isNotEmpty())
                            reminderDateTime.date
                        else "Select Date",
                        fontSize = 15.sp,
                        color = if (reminderDateTime.date.isNotEmpty())
                            colorResource(id = R.color.nice_blue)
                        else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Picker Button
            OutlinedCard(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            val selectedTime = timeFormat.format(calendar.time)
                            reminderDateTime = reminderDateTime.copy(
                                time = selectedTime,
                                timestamp = calendar.timeInMillis
                            )

                            // If date is also set, trigger the callback
                            if (reminderDateTime.date.isNotEmpty()) {
                                val updatedDateTime = reminderDateTime.copy(isSet = true)
                                reminderDateTime = updatedDateTime
                                onReminderSet(updatedDateTime)
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false // Use 12-hour format
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (reminderDateTime.time.isNotEmpty())
                        colorResource(id = R.color.nice_blue).copy(alpha = 0.05f)
                    else Color.Transparent
                ),
                border = BorderStroke(
                    1.dp,
                    if (reminderDateTime.time.isNotEmpty())
                        colorResource(id = R.color.nice_blue).copy(alpha = 0.3f)
                    else Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = if (reminderDateTime.time.isNotEmpty())
                            colorResource(id = R.color.nice_blue)
                        else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (reminderDateTime.time.isNotEmpty())
                            reminderDateTime.time
                        else "Select Time",
                        fontSize = 15.sp,
                        color = if (reminderDateTime.time.isNotEmpty())
                            colorResource(id = R.color.nice_blue)
                        else Color.Gray
                    )
                }
            }

            // Show confirmation message when both date and time are set
            if (reminderDateTime.isSet) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.nice_blue).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            color = colorResource(id = R.color.nice_blue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Reminder set for ${reminderDateTime.date} at ${reminderDateTime.time}",
                            fontSize = 13.sp,
                            color = colorResource(id = R.color.nice_blue),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Helper text
            if (!reminderDateTime.isSet) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select both date and time to set reminder",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Extension function to get timestamp from ReminderDateTime
fun ReminderDateTime.getTimestampOrNull(): Long? {
    return if (isSet && timestamp > 0) timestamp else null
}

// Extension function to create ReminderDateTime from timestamp
fun Long.toReminderDateTime(): ReminderDateTime {
    val calendar = Calendar.getInstance().apply { timeInMillis = this@toReminderDateTime }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    return ReminderDateTime(
        date = dateFormat.format(calendar.time),
        time = timeFormat.format(calendar.time),
        timestamp = this,
        isSet = true
    )
}