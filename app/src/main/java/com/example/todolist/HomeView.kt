package com.example.todolist

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.todolist.data.DummyTask
import com.example.todolist.data.Task

@Composable
fun HomeView(navController: NavHostController, viewModel: TaskViewModel) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBarView(title = "Today") },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp),
                contentColor = Color.White,
                backgroundColor = colorResource(id = R.color.dark_blue),
                onClick = {
                    taskBeingEdited = null
                    showBottomSheet = true
                    Toast.makeText(context, "Add a task here!", Toast.LENGTH_LONG).show()
                }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }

    ) {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            items(DummyTask.taskList) {
                task ->
                TaskItem(task)
            }
        }
    }

    if (showBottomSheet) {
        AddTaskView(
            0L,
            viewModel,
            onDismiss = {
                showBottomSheet = false
                taskBeingEdited = null
            },
            onSubmit = { task ->
                if (taskBeingEdited == null) {
                    //
                } else {
                    //
                }
            }
        )
    }
}

@Composable
fun TaskItem(task: Task) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
        .clickable {
            // Add the navigation logic here
        },
        backgroundColor = Color.White

    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Checkbox in the top-left corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = null, // disables interaction
                    enabled = false // grays it out to show it's inactive
                )
            }

            // Task title and description
            Text(
                text = task.title,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.body2
            )

            // Separator Line
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                thickness = 1.dp
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    MaterialTheme {
        TaskItem(
            task = Task(
                title = "Buy groceries",
                description = "Milk, eggs, bread, and cheese"
            )
        )
    }
}