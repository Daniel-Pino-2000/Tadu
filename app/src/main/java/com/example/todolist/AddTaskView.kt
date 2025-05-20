package com.example.todolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
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
import java.util.Date


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
        viewModel.taskDateState = task.value.date
        viewModel.taskAddressState = task.value.address
        viewModel.taskDeadline = task.value.deadline
        viewModel.taskPriority = task.value.priority
    }
    else {
        viewModel.taskTitleState = ""
        viewModel.taskDescriptionState = ""
        viewModel.taskDateState = ""
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
                value = viewModel.taskDateState,
                onValueChange = { viewModel.onTaskDateChanged(it) },
                label = { Text("Date") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.taskAddressState,
                onValueChange = { viewModel.onAddressChanged(it) },
                label = { Text("Address") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.taskDeadline,
                onValueChange = { viewModel.onTaskDeadlineChanged(it) },
                label = { Text("Deadline") }
            )
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
                                date = viewModel.taskDateState,
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
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}