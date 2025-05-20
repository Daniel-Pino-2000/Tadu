package com.example.todolist

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.todolist.data.DummyTask
import com.example.todolist.data.Task

@Composable
fun HomeView(navController: NavHostController, viewModel: TaskViewModel) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf(false) }
    var id by remember { mutableLongStateOf(0L) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBarView(title = "Today") },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp),
                contentColor = Color.White,
                backgroundColor = colorResource(id = R.color.dark_blue),
                onClick = {
                    taskBeingEdited = false
                    showBottomSheet = true
                    id = 0L
                    Toast.makeText(context, "Add a task here!", Toast.LENGTH_LONG).show()
                }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }

    ) {
        val taskList = viewModel.getAllTasks.collectAsState(initial = listOf())
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            items(taskList.value, key = {task-> task.id}) {
                task ->
                TaskItem(task, viewModel, 1) {
                    id = task.id
                    taskBeingEdited = true
                    showBottomSheet = true
                }
            }
        }
    }

    if (showBottomSheet) {
        AddTaskView(
            id,
            viewModel,
            onDismiss = {
                showBottomSheet = false
                taskBeingEdited = false
            },
            onSubmit = { task ->
                if (taskBeingEdited == false) {
                    viewModel.addTask(task)  // ← Add to DB
                } else {
                    Toast.makeText(context, "It is being updated!", Toast.LENGTH_LONG).show()
                    viewModel.updateTask(task)  // ← Update in DB

                }
                showBottomSheet = false
                taskBeingEdited = false

            }
        )
    }
}

@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel, mode: Int, onClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }

    val elevationValue = if (mode == 1) 8.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .clickable { onClick() },
        backgroundColor = colorResource(id = R.color.light_gray),
        elevation = elevationValue
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Spacer(modifier = Modifier.width(6.dp))

            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    isChecked = checked
                    if (checked) {
                        viewModel.deleteTask(task)
                    }
                },
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.body2
                )

                if (mode == 2) {
                    Divider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

