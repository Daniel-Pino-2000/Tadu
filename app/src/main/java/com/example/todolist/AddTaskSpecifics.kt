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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScrollableRow(viewModel: TaskViewModel, isHistoryMode: Boolean) {

    var scrollState = rememberScrollState()

    Row(modifier = Modifier.horizontalScroll(scrollState).height(76.dp)
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {

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
            readOnly = isHistoryMode,
            trailingIcon = {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        openAddressInMaps(context, viewModel.taskAddressState)
                    },
                    enabled = viewModel.taskAddressState.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Open in Maps",
                        tint = if (viewModel.taskAddressState.isNotBlank())
                            Color.Blue
                        else
                            Color.LightGray
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = colorResource(id = R.color.nice_blue),
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.width(16.dp))

        DropUpPriorityButton(viewModel, isHistoryMode)

        Spacer(modifier = Modifier.width(16.dp))

        DeadlinePickerButton(viewModel, isHistoryMode) { selectedDate ->


            viewModel.onTaskDeadlineChanged(selectedDate)


        }



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