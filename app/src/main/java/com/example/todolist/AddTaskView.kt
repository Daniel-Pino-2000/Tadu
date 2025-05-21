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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date


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
        val task = viewModel.getTaskById(id).collectAsState(initial = Task(0L, "", "", "", "", "5", ""))
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

            OutlinedTextField(
                value = viewModel.taskAddressState,
                onValueChange = { viewModel.onAddressChanged(it) },
                label = { Text("Address") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            DeadlinePickerButton { selectedDate ->

                val formatter = DateTimeFormatter.ofPattern("MMM dd")
                val currentYear = LocalDate.now().year

                // Parse the selectedDate string to LocalDate by appending current year
                val parsedDate = LocalDate.parse("$selectedDate $currentYear", DateTimeFormatter.ofPattern("MMM dd yyyy"))

                val today = LocalDate.now()
                val yesterday = today.minusDays(1)
                val tomorrow = today.plusDays(1)

                val label = when (parsedDate) {
                    today -> "Today"
                    yesterday -> "Yesterday"
                    tomorrow -> "Tomorrow"
                    else -> selectedDate
                }

                viewModel.onTaskDeadlineChanged(label)


            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.taskPriority,
                onValueChange = { viewModel.onTaskPriorityChanged(it) },
                label = { Text("Priority") }
            )
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
fun DeadlinePickerButton(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Keep track of whether to show the dialog
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

    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White  // Set the button background color
        )
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = colorResource(id = R.color.nice_blue))
        Spacer(modifier = Modifier.padding(end = 2.dp))
        Text("Deadline", color = colorResource(id = R.color.nice_blue))
    }
}
