package com.myapp.tadu

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import com.myapp.tadu.ui.theme.LocalDynamicColors
import java.time.ZoneId
import java.util.Locale
import androidx.compose.material3.MaterialTheme
import com.myapp.tadu.view_model.TaskViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScrollableRow(viewModel: TaskViewModel, isHistoryMode: Boolean) {
    var scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .height(76.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Address field
        OutlinedTextField(
            value = viewModel.taskAddressState,
            onValueChange = { viewModel.onAddressChanged(it) },
            maxLines = 1,
            label = {
                Text(
                    "Address",
                    color = if (isHistoryMode)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            enabled = !isHistoryMode,
            trailingIcon = {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        if (!isHistoryMode) {
                            openAddressInMaps(context, viewModel.taskAddressState)
                        }
                    },
                    enabled = viewModel.taskAddressState.isNotBlank() && !isHistoryMode
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Open in Maps",
                        tint = if (viewModel.taskAddressState.isNotBlank())
                            LocalDynamicColors.current.niceColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = LocalDynamicColors.current.niceColor,
                focusedBorderColor = LocalDynamicColors.current.niceColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Priority button
        DropUpPriorityButton(viewModel, isHistoryMode)

        Spacer(modifier = Modifier.width(16.dp))

        // Deadline button
        DeadlinePickerButton(viewModel, isHistoryMode) { selectedDate ->
            viewModel.onTaskDeadlineChanged(selectedDate)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Label button
        LabelButton(viewModel, isHistoryMode)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeadlinePickerButton(
    viewModel: TaskViewModel,
    isHistoryMode: Boolean,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDialog by remember { mutableStateOf(false) }

    // Launch DatePickerDialog only once when showDialog is true
    LaunchedEffect(showDialog) {
        if (showDialog) {
            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                    val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
                    val selectedDate = selectedLocalDate.format(formatter)
                    onDateSelected(selectedDate)
                },
                year, month, day
            ).apply {
                setOnCancelListener {
                    showDialog = false
                }
                setOnDismissListener {
                    showDialog = false
                }
            }.show()

            showDialog = false
        }
    }

    // Use OutlinedButton to match OutlinedTextField appearance
    OutlinedButton(
        onClick = { if (!isHistoryMode) showDialog = true },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        enabled = !isHistoryMode,
        modifier = Modifier.height(62.dp).padding(top = 7.dp).focusable(false),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color.Transparent,
            contentColor = LocalDynamicColors.current.niceColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Icon(Icons.Default.Alarm, contentDescription = null, tint = LocalDynamicColors.current.niceColor)
        Spacer(modifier = Modifier.width(4.dp))

        if (viewModel.taskDeadline.isEmpty()) {
            Text("Deadline")
        } else {
            Text(viewModel.taskDeadline)

            // Enhanced delete button with better design
            if (!isHistoryMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        .clickable {
                            viewModel.onTaskDeadlineChanged("")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear deadline",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DropUpPriorityButton(viewModel: TaskViewModel, isHistoryMode: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    val red = colorResource(id = R.color.red_yesterday)
    val orange = colorResource(id = R.color.orange)
    val blue = colorResource(id = R.color.blue_today)
    val gray = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val priority = viewModel.taskPriority

    // Determine icon text and tint based on priority
    val (iconTint, labelText) = when (priority) {
        "1" -> Pair(red, "Priority 1")
        "2" -> Pair(orange, "Priority 2")
        "3" -> Pair(blue, "Priority 3")
        "4" -> Pair(gray, "Priority 4")
        else -> Pair(LocalDynamicColors.current.niceColor, "Priority")
    }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            enabled = !isHistoryMode,
            modifier = Modifier
                .height(62.dp)
                .focusable(false)
                .padding(top = 7.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = LocalDynamicColors.current.niceColor,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = iconTint)
            Spacer(modifier = Modifier.width(4.dp))
            Text(labelText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-160).dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("1")
            }) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = red)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Priority 1",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("2")
            }) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = orange)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Priority 2",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("3")
            }) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = blue)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Priority 3",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("4")
            }) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Priority 4",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun addTaskToCalendar(context: Context, title: String, deadline: String) {
    try {

        val deadlineWithYear = deadline

        val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
        val parsedDate = LocalDate.parse(deadlineWithYear, formatter)

        val startMillis = parsedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Primary approach: Calendar app intent with proper MIME type
        val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            type = "vnd.android.cursor.dir/event"
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000)
        }


        // Now resolveActivity should work better with the queries declaration
        val resolvedActivity = calendarIntent.resolveActivity(context.packageManager)
        if (resolvedActivity != null) {
            context.startActivity(calendarIntent)
            return
        }

        // Fallback approaches if primary fails
        val success = trySpecificCalendarApps(context, title, startMillis) ||
                tryGenericCalendarIntent(context, title, startMillis)

        if (!success) {
            Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        Log.e("AddToCalendar", "Failed to parse date or launch calendar", e)
        Toast.makeText(context, "Failed to add event to calendar", Toast.LENGTH_SHORT).show()
    }
}

private fun tryLaunchIntent(context: Context, intent: Intent): Boolean {
    return try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        Log.d("AddToCalendar", "Primary intent failed: ${e.message}")
        false
    }
}

private fun trySpecificCalendarApps(context: Context, title: String, startMillis: Long): Boolean {
    val calendarApps = listOf(
        "com.google.android.calendar", // Google Calendar
        "com.samsung.android.calendar", // Samsung Calendar
        "com.android.calendar",         // AOSP Calendar
        "com.htc.calendar",            // HTC Calendar
        "com.lge.calendar",            // LG Calendar
        "com.miui.calendar",           // Xiaomi Calendar
        "com.huawei.calendar"          // Huawei Calendar
    )

    for (packageName in calendarApps) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                type = "vnd.android.cursor.dir/event"
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000)
            }

            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.d("AddToCalendar", "Failed to launch $packageName: ${e.message}")
        }
    }
    return false
}

private fun tryGenericCalendarIntent(context: Context, title: String, startMillis: Long): Boolean {
    return try {
        // Fallback: Use ACTION_EDIT instead of ACTION_INSERT
        val editIntent = Intent(Intent.ACTION_EDIT).apply {
            data = CalendarContract.Events.CONTENT_URI
            type = "vnd.android.cursor.dir/event"
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000)
        }
        context.startActivity(editIntent)
        true
    } catch (e: Exception) {
        Log.d("AddToCalendar", "Generic calendar intent failed: ${e.message}")

        // Last resort: Try to open any calendar app without pre-filling data
        try {
            val basicIntent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_APP_CALENDAR)
            }
            context.startActivity(basicIntent)
            Toast.makeText(context, "Calendar opened - please add event manually", Toast.LENGTH_LONG).show()
            true
        } catch (e2: Exception) {
            Log.d("AddToCalendar", "Basic calendar intent failed: ${e2.message}")
            false
        }
    }
}

// Label Button Component
@Composable
fun LabelButton(
    viewModel: TaskViewModel,
    isHistoryMode: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showNewLabelDialog by remember { mutableStateOf(false) }
    val availableLabels by viewModel.getAllLabels.collectAsState(initial = emptyList())

    // Get display text for button
    val displayText = when {
        viewModel.taskLabel.isEmpty() -> "Label"
        else -> viewModel.taskLabel
    }

    // Color based on whether labels are selected
    val iconTint = if (viewModel.taskLabel.isNotEmpty())
        LocalDynamicColors.current.niceColor
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Box {
        OutlinedButton(
            onClick = { if (!isHistoryMode) expanded = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            enabled = !isHistoryMode,
            modifier = modifier
                .height(62.dp)
                .focusable(false)
                .padding(top = 7.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = LocalDynamicColors.current.niceColor,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(displayText)

            // Enhanced delete button with better design
            if (viewModel.taskLabel.isNotEmpty() && !isHistoryMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        .clickable {
                            viewModel.onTaskLabelsChanged("")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear label",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-200).dp),
            modifier = Modifier
                .widthIn(min = 200.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Show available labels
            if (availableLabels.isNotEmpty()) {
                availableLabels.forEach { label ->
                    if (label.isNotEmpty()) {
                        DropdownMenuItem(
                            onClick = {
                                viewModel.onTaskLabelsChanged(label)
                                expanded = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                tint = LocalDynamicColors.current.niceColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                label,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            }

            // Add new label option
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    showNewLabelDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = LocalDynamicColors.current.niceColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create new label",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // New Label Dialog
    if (showNewLabelDialog) {
        NewLabelDialog(
            onDismiss = { showNewLabelDialog = false },
            onConfirm = { newLabel ->
                viewModel.onTaskLabelsChanged(newLabel)
                showNewLabelDialog = false
            }
        )
    }
}

// New Label Dialog Component
@Composable
fun NewLabelDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var labelText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 320.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create new label",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = {
                        Text(
                            "Label name",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = LocalDynamicColors.current.niceColor,
                        focusedBorderColor = LocalDynamicColors.current.niceColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (labelText.isNotBlank()) {
                                onConfirm(labelText.trim())
                            }
                        }
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = LocalDynamicColors.current.niceColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = {
                            if (labelText.isNotBlank()) {
                                onConfirm(labelText.trim())
                            }
                        },
                        enabled = labelText.isNotBlank(),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Add",
                            color = if (labelText.isNotBlank())
                                LocalDynamicColors.current.niceColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}