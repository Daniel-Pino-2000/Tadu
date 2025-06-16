package com.example.todolist

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScrollableRow(viewModel: TaskViewModel) {

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

        DropUpMenuButton(viewModel)

        Spacer(modifier = Modifier.width(16.dp))

        DeadlinePickerButton { selectedDate ->


            viewModel.onTaskDeadlineChanged(selectedDate)


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
        modifier = Modifier.height(62.dp).padding(top = 7.dp).focusable(false),  // Match typical TextField height
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
            modifier = Modifier.height(62.dp).focusable(false).padding(top = 7.dp).onGloballyPositioned { coordinates ->
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

        ) {
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 1
                viewModel.onTaskPriorityChanged("1")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = colorResource(id = R.color.red_yesterday))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 1")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 2
                viewModel.onTaskPriorityChanged("2")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = colorResource(id = R.color.orange))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 2")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 3
                viewModel.onTaskPriorityChanged("3")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null, tint = colorResource(id = R.color.blue_today))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 3")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                // Handle Option 4
                viewModel.onTaskPriorityChanged("4")
            }) {
                Icon(PriorityUtils.priorityIcon, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Priority 4")
            }
        }
    }
}