package com.example.todolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
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
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember {mutableStateOf(Date().toString())}
    var address by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = if (id == 0L) "Add Task" else "Edit Task")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {title = it},
                label = {Text("Title")}
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSubmit(Task(id, title, description, date, address))
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        }
    }
}