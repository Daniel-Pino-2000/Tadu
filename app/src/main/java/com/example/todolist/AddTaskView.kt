package com.example.todolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Task
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskView(
    id: Long,
    viewModel: TaskViewModel,
    onDismiss: () -> Unit,
    onSubmit: (task: Task) -> Unit
) {

    if (id != 0L) {
        val task = viewModel.getTaskById(id).collectAsState(initial = Task(0L, "", "", "", "", "", ""))
        viewModel.taskTitleState = task.value.title
        viewModel.taskDescriptionState = task.value.description
        viewModel.taskAddressState = task.value.address
        viewModel.taskDeadline = task.value.deadline
        viewModel.taskPriority = task.value.priority
    }
    else {
        viewModel.taskTitleState = ""
        viewModel.taskDescriptionState = ""
        viewModel.taskAddressState = ""
        viewModel.taskDeadline = ""
        viewModel.taskPriority = ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = if (id == 0L) "Add Task" else "Edit Task")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.taskTitleState,
                onValueChange = {viewModel.onTaskTitleChanged(it)},
                label = {Text("Title")}
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.taskDescriptionState,
                onValueChange = { viewModel.onTaskDescriptionChanged(it) },
                label = { Text("Description") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScrollableRow(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (id == 0L) {
                        onSubmit(
                            Task(
                                title = viewModel.taskTitleState,
                                description = viewModel.taskDescriptionState,
                                address = viewModel.taskAddressState,
                                priority = viewModel.taskPriority,
                                deadline = viewModel.taskDeadline
                            )
                        )
                    }

                    else {
                        onSubmit(
                            Task(
                                id = id,
                                title = viewModel.taskTitleState,
                                description = viewModel.taskDescriptionState,
                                date = viewModel.taskDateState,
                                address = viewModel.taskAddressState,
                                priority = viewModel.taskPriority,
                                deadline = viewModel.taskDeadline
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.nice_blue) // Set the button background color
                )


            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScrollableRow(viewModel: TaskViewModel) {

    var scrollState = rememberScrollState()

    Row(modifier = Modifier.horizontalScroll(scrollState).height(76.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {

        OutlinedTextField(
            value = viewModel.taskAddressState,
            onValueChange = { viewModel.onAddressChanged(it) },
            label = { Text("Address") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        try {
                            val encodedLocation = Uri.encode(viewModel.taskAddressState.trim())
                            val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            // Check if Google Maps is available
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                // Fallback to any available maps app
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(fallbackIntent)
                            }
                        } catch (e: Exception) {
                            // Handle the exception (e.g., show a toast or log)
                            Log.e("MapsLaunch", "Failed to open maps", e)
                            // Optionally show a toast to the user
                            Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
                        }
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
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.width(16.dp))

        DeadlinePickerButton { selectedDate ->


            viewModel.onTaskDeadlineChanged(selectedDate)


        }

        Spacer(modifier = Modifier.width(16.dp))

        DropUpMenuButton(viewModel)

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeadlinePickerButton(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                val formatter = DateTimeFormatter.ofPattern("MMM dd")
                val selectedDate = selectedLocalDate.format(formatter)
                onDateSelected(selectedDate)
                showDialog = false
            },
            year, month, day
        ).show()
    }

    // Use OutlinedButton to match OutlinedTextField appearance
    OutlinedButton(
        onClick = { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.height(62.dp).padding(top = 7.dp),  // Match typical TextField height
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color.Transparent,  // No background, like OutlinedTextField
            contentColor = Color.Blue
        )
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Blue)
        Spacer(modifier = Modifier.width(4.dp))
        Text("Deadline")
    }
}

@Composable
fun DropUpMenuButton(viewModel: TaskViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val buttonWidth = remember { mutableStateOf(0) }

    Box {
        // Button that toggles the menu
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier.height(62.dp).padding(top = 7.dp).onGloballyPositioned { coordinates ->
                buttonWidth.value = coordinates.size.width
            },  // Match typical TextField height
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = Color.Transparent,  // No background, like OutlinedTextField
                contentColor = Color.Blue
            )
        ) {
            Icon(PriorityUtils.priorityIcon, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Priority")
        }

        // DropdownMenu (simulating DropUp)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-160).dp),  // negative y offset moves it up
            modifier = Modifier.width(with(LocalDensity.current) { buttonWidth.value.toDp() })
        ) {
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 1
                viewModel.onTaskPriorityChanged("1")
            }) {
                Text("Option 1")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 2
                viewModel.onTaskPriorityChanged("2")
            }) {
                Text("Option 2")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 3
                viewModel.onTaskPriorityChanged("3")
            }) {
                Text("Option 3")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 4
                viewModel.onTaskPriorityChanged("4")
            }) {
                Text("Option 4")
            }
        }
    }
}


