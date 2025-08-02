package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AppBarView(
    title: String,
    navController: NavHostController
) {
    var expanded by remember { mutableStateOf(false) }


    TopAppBar(
        title = {
            Text(
                text = title,
                color = colorResource(id = R.color.black),
                style = MaterialTheme.typography.h6, // Larger and bolder by default
                modifier = Modifier.padding(start = 8.dp)
            )

        },
        elevation = 0.dp,
        backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        actions = {
            IconButton(
                onClick = {
                    expanded = true
                },

                enabled = true
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropDownActionMenu(expanded = expanded, navController, onDismissRequest = { expanded = false })

        },

    )
}

@Composable
fun DropDownActionMenu(expanded: Boolean = true, navController: NavHostController, onDismissRequest: () -> Unit) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        // offset = DpOffset(x = 0.dp, y = 0.dp)
    ) {
        DropdownMenuItem(onClick = {
            onDismissRequest()
            navController.navigate(Screen.History.route)
        }) {
            Icon(Icons.Outlined.History, contentDescription = null, tint = colorResource(id = R.color.dropMenuIcon_gray))
            Spacer(Modifier.padding(end = 5.dp))
            Text("Task History")
        }

        DropdownMenuItem(onClick = {
            onDismissRequest()
            navController.navigate(Screen.Calendar.route)
        }) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = colorResource(id = R.color.dropMenuIcon_gray))
            Spacer(Modifier.padding(end = 5.dp))
            Text("Calendar View")
        }

        DropdownMenuItem(onClick = {
            onDismissRequest()
            navController.navigate(Screen.Reminders.route)
        }) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = colorResource(id = R.color.dropMenuIcon_gray))
            Spacer(Modifier.padding(end = 5.dp))
            Text("Reminders")
        }

    }
}