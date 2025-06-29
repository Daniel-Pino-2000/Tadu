package com.example.todolist

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todolist.data.Task
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface



@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeView(navController: NavHostController, viewModel: TaskViewModel) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val taskToUpdate = remember { mutableStateOf(Task(0, "", "", "", "4", "")) } // Dummy task

    var id by remember { mutableLongStateOf(0L) }

    val currentScreen = remember {
        viewModel.currentScreen.value
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBar: @Composable () -> Unit = {
        if (currentScreen is Screen.BottomScreen.Today ||
            currentScreen is Screen.BottomScreen.Inbox ||
            currentScreen is Screen.BottomScreen.Search) {

            // Modern Navigation Bar with glassmorphism effect
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    // Glassmorphism background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    )

                    // Navigation items
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        screenInBottom.forEach { item ->
                            val isSelected = currentRoute == item.bRoute
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.1f else 1f,
                                animationSpec = tween(300),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .scale(scale)
                                    .clickable {
                                        if (currentRoute != item.bRoute) {
                                            navController.navigate(item.bRoute) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        colorResource(id = R.color.nice_blue).copy(alpha = 0.2f),
                                                        Color.Transparent
                                                    ),
                                                    radius = 60f
                                                ),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Icon with animated container
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .then(
                                                if (isSelected) {
                                                    Modifier.background(
                                                        colorResource(id = R.color.nice_blue),
                                                        CircleShape
                                                    )
                                                } else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.bTitle,
                                            tint = if (isSelected) Color.White else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Label with animated color
                                    Text(
                                        text = item.bTitle,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) colorResource(id = R.color.nice_blue) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = bottomBar,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = { AppBarView(title = getScreenTitle(currentRoute)) },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                containerColor = colorResource(id = R.color.nice_blue),
                onClick = {
                    taskBeingEdited = false
                    showBottomSheet = true
                    id = 0L
                }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }

    ) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
        val currentYear = today.year

        val taskList = viewModel.getAllTasks.collectAsState(initial = listOf())
        val tasks = taskList.value

        val tasksToDisplay = when (currentRoute) {
            "today" -> tasks.filter { task ->
                if (task.deadline.isBlank()) {
                    true // include tasks with blank deadline in "today"
                } else {
                    try {
                        val taskDate = LocalDate.parse("${task.deadline} $currentYear", formatter)
                        !taskDate.isAfter(today) // deadline <= today
                    } catch (e: Exception) {
                        false // exclude if parsing fails
                    }
                }
            }
            "inbox" -> tasks.filter { task ->
                try {
                    val taskDate = LocalDate.parse("${task.deadline} $currentYear", formatter)
                    taskDate.isAfter(today) // deadline > today
                } catch (e: Exception) {
                    false // exclude if parsing fails
                }
            }
            else -> tasks // fallback: show all tasks
        }



        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            // Sorts the tasks by priority
            val sortedTasks = tasksToDisplay.sortedBy {
                (if (it.priority.isBlank()) "4" else it.priority).toInt()
            }

            items(sortedTasks, key = { task -> task.id }) { task ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        when (dismissValue) {
                            DismissValue.DismissedToEnd -> {
                                taskToUpdate.value = task
                                // Show date picker (do not dismiss)
                                showDatePicker.value = true
                                false // prevent item from being swiped off screen
                            }
                            DismissValue.DismissedToStart -> {
                                // Delete the task
                                viewModel.deleteTask(task)
                                true // allow swipe to delete
                            }
                            else -> false
                        }
                    }
                )

                Box(modifier = Modifier.padding(top = 8.dp)) {
                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val color by animateColorAsState(
                                targetValue = when (dismissState.targetValue) {
                                    DismissValue.DismissedToStart -> Color.Red
                                    DismissValue.DismissedToEnd -> colorResource(id = R.color.orange)
                                    else -> Color.Transparent
                                },
                                label = ""
                            )

                            val alignment = when (dismissState.targetValue) {
                                DismissValue.DismissedToStart -> Alignment.CenterEnd
                                DismissValue.DismissedToEnd -> Alignment.CenterStart
                                else -> Alignment.Center
                            }

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                when (dismissState.targetValue) {
                                    DismissValue.DismissedToStart -> Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        tint = Color.White
                                    )

                                    DismissValue.DismissedToEnd -> Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Date Icon",
                                        tint = Color.White
                                    )

                                    else -> {}
                                }
                            }
                        },
                        directions = setOf(
                            DismissDirection.EndToStart,
                            DismissDirection.StartToEnd
                        ),
                        dismissThresholds = { FractionalThreshold(0.1f) },
                        dismissContent = {
                            TaskItem(task, viewModel) {
                                id = task.id
                                taskBeingEdited = true
                                showBottomSheet = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDatePicker.value) {
        DatePicker { selectedDate ->
            viewModel.onTaskDeadlineChanged(selectedDate)
            val newTask = Task(
                id = taskToUpdate.value.id,
                title = taskToUpdate.value.title,
                description = taskToUpdate.value.description,
                date = taskToUpdate.value.date,
                address = taskToUpdate.value.address,
                priority = taskToUpdate.value.priority,
                deadline = selectedDate
            )
            viewModel.updateTask(newTask) // When the user picks a date, update the task:
        }
        showDatePicker.value = false
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
                    viewModel.updateTask(task)  // ← Update in DB
                }
                showBottomSheet = false
                taskBeingEdited = false
            }
        )
    }
}

// Option 2: More elegant approach using your screenInBottom list
fun getScreenTitle(route: String?): String {
    return screenInBottom.find { it.bRoute == route }?.bTitle ?: "Today"
}