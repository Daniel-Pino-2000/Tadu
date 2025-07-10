package com.example.todolist

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.runtime.LaunchedEffect
import java.time.ZoneId
import java.util.Locale

// Update your ScrollableRow function to include the label button
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
            label = { Text("Address") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray
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
                            Color.Blue else Color.LightGray
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = colorResource(id = R.color.nice_blue),
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black,
                disabledTextColor = Color.Black,
                disabledLabelColor = Color.Black,
                disabledLeadingIconColor = Color.Gray,
                disabledTrailingIconColor = Color.LightGray
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

        // New Label button
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
                    val formatter = DateTimeFormatter.ofPattern("MMM dd")
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

            // Ensure this gets reset even if selection listener doesn’t trigger (e.g., user taps outside)
            // This line is technically redundant due to listeners above, but safe to keep
            showDialog = false
        }
    }

    // Use OutlinedButton to match OutlinedTextField appearance
    OutlinedButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        enabled = !isHistoryMode,
        modifier = Modifier.height(62.dp).padding(top = 7.dp).focusable(false),  // Match typical TextField height
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color.Transparent,  // No background, like OutlinedTextField
            contentColor = Color.Blue
        )
    ) {
        Icon(Icons.Default.Alarm, contentDescription = null, tint = Color.Blue)
        Spacer(modifier = Modifier.width(4.dp))

        if (viewModel.taskDeadline.isEmpty()) {
            Text("Deadline")
        }
        else {
            Text(viewModel.taskDeadline)
        }
    }
}

@Composable
fun DropUpPriorityButton(viewModel: TaskViewModel, isHistoryMode: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val buttonWidth = remember { mutableStateOf(0) }

    val red = colorResource(id = R.color.red_yesterday)
    val orange = colorResource(id = R.color.orange)
    val blue = colorResource(id = R.color.blue_today)
    val black = Color.Black

    // Read current priority from viewModel (you might use StateFlow, LiveData, etc.)
    val priority = viewModel.taskPriority // Replace with your actual priority state

    // Determine icon text and tint based on priority
    val (iconTint, labelText) = when (priority) {
        "1" -> Pair(red, "Priority 1")
        "2" -> Pair(orange, "Priority 2")
        "3" -> Pair(blue, "Priority 3")
        "4" -> Pair(black, "Priority 4")
        else -> Pair(Color.Blue, "Priority")
    }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Black),
            enabled = !isHistoryMode,
            modifier = Modifier
                .height(62.dp)
                .focusable(false)
                .padding(top = 7.dp)
                .onGloballyPositioned { coordinates ->
                    buttonWidth.value = coordinates.size.width
                },
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = Color.Blue
            )
        ) {
            Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = iconTint)
            Spacer(modifier = Modifier.width(4.dp))
            Text(labelText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-160).dp)
        ) {
            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("1")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = red)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 1")
            }

            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("2")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = orange)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 2")
            }

            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("3")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = blue)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 3")
            }

            DropdownMenuItem(onClick = {
                expanded = false
                viewModel.onTaskPriorityChanged("4")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 4")
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun addTaskToCalendar(context: Context, title: String, deadline: String) {
    try {
        // Append current year to deadline like "Jun 16" → "Jun 16 2025"
        val currentYear = LocalDate.now().year
        val deadlineWithYear = "$deadline $currentYear"

        // Parse deadline using formatter for "MMM dd yyyy"
        val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
        val parsedDate = LocalDate.parse(deadlineWithYear, formatter)

        val startMillis = parsedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000) // 1 hour
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        Log.e("AddToCalendar", "Failed to parse date or launch calendar", e)
        Toast.makeText(context, "Failed to add event to calendar", Toast.LENGTH_SHORT).show()
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
    val availableLabels by viewModel.availableLabels.collectAsState()

    // Get display text for button
    val displayText = when {
        viewModel.taskLabels.isEmpty() -> "Labels"
        viewModel.taskLabels.size == 1 -> viewModel.taskLabels.first()
        else -> "${viewModel.taskLabels.size} labels"
    }

    // Color based on whether labels are selected
    val iconTint = if (viewModel.taskLabels.isNotEmpty())
        colorResource(id = R.color.nice_blue) else Color.Gray

    Box {
        OutlinedButton(
            onClick = { if (!isHistoryMode) expanded = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Black),
            enabled = !isHistoryMode,
            modifier = modifier
                .height(62.dp)
                .focusable(false)
                .padding(top = 7.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = Color.Transparent,
                contentColor = Color.Blue
            )
        ) {
            Icon(
                imageVector = Icons.Default.Label, // You'll need to import this
                contentDescription = null,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(displayText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-200).dp),
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            // Show selected labels first
            if (viewModel.taskLabels.isNotEmpty()) {
                viewModel.taskLabels.forEach { label ->
                    DropdownMenuItem(
                        onClick = {
                            // Remove label
                            val newLabels = viewModel.taskLabels.toMutableList()
                            newLabels.remove(label)
                            viewModel.onTaskLabelsChanged(newLabels)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colorResource(id = R.color.nice_blue),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }

                if (availableLabels.isNotEmpty()) {
                    Divider()
                }
            }

            // Show available labels that aren't selected
            availableLabels.filter { it !in viewModel.taskLabels }.forEach { label ->
                DropdownMenuItem(
                    onClick = {
                        // Add label
                        val newLabels = viewModel.taskLabels.toMutableList()
                        newLabels.add(label)
                        viewModel.onTaskLabelsChanged(newLabels)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }

            // Add new label option
            Divider()
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    showNewLabelDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = colorResource(id = R.color.nice_blue),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create new label", fontWeight = FontWeight.Medium)
            }
        }
    }

    // New Label Dialog
    if (showNewLabelDialog) {
        NewLabelDialog(
            onDismiss = { showNewLabelDialog = false },
            onConfirm = { newLabel ->
                val newLabels = viewModel.taskLabels.toMutableList()
                newLabels.add(newLabel)
                viewModel.onTaskLabelsChanged(newLabels)
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
                .fillMaxSize()
                .background(Color(0x80000000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 320.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create new label",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Label name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = colorResource(id = R.color.nice_blue),
                        focusedBorderColor = colorResource(id = R.color.nice_blue),
                        unfocusedBorderColor = Color.Gray
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
                            color = colorResource(id = R.color.nice_blue),
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
                                colorResource(id = R.color.nice_blue) else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}