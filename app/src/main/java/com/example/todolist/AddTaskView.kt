package com.example.todolist

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TextButton
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


/**
 * A composable that displays a modal bottom sheet for adding or editing tasks.
 *
 * Features:
 * - Confirmation dialog when dismissing with unsaved changes
 * - Handles back button, swipe-to-dismiss, and tap-outside-to-dismiss consistently
 * - Auto-focuses title field and shows keyboard when opened
 * - Validates input and disables submit for invalid tasks
 *
 * @param id The task ID (0L for new task, existing ID for editing)
 * @param viewModel The TaskViewModel that manages task state
 * @param onDismiss Callback invoked when the sheet is dismissed
 * @param onSubmit Callback invoked when a valid task is submitted
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskView(
    id: Long,
    viewModel: TaskViewModel,
    onDismiss: () -> Unit,
    onSubmit: (task: Task) -> Unit
) {
    // State for controlling the confirmation dialog visibility
    val openConfirmDialog = remember { mutableStateOf(false) }

    // Flag to control when dismissal should be allowed (bypasses confirmation)
    val allowDismiss = remember { mutableStateOf(false) }

    // Configure the bottom sheet state with custom dismiss behavior
    var sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // When trying to hide the sheet, check if there are unsaved changes
            if (newValue == SheetValue.Hidden && viewModel.taskHasBeenChanged && !allowDismiss.value) {
                openConfirmDialog.value = true // Show confirmation dialog
                false // Prevent the sheet from closing
            } else {
                true // Allow the state change (sheet can close)
            }
        }
    )

    // Focus management for automatic keyboard display
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle hardware/gesture back button presses
    // Ensures consistent behavior with swipe-to-dismiss and tap-outside-to-dismiss
    BackHandler(enabled = true) {
        if (viewModel.taskHasBeenChanged && !allowDismiss.value) {
            openConfirmDialog.value = true // Show confirmation if there are unsaved changes
        } else {
            onDismiss() // Allow immediate dismissal if no changes or dismissal is allowed
        }
    }

    // Auto-focus the title field and show keyboard when sheet becomes visible
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Initialize form fields based on whether we're editing an existing task or creating a new one
    if (id != 0L) {
        // Editing existing task - populate fields with current values
        val task = viewModel.getTaskById(id).collectAsState(initial = Task(0L, "", "", "", "", "4", ""))
        viewModel.taskTitleState = task.value.title
        viewModel.taskDescriptionState = task.value.description
        viewModel.taskAddressState = task.value.address
        viewModel.taskDeadline = task.value.deadline
        viewModel.taskPriority = task.value.priority
    }
    else {
        // Creating new task - reset fields to defaults
        viewModel.taskTitleState = ""
        viewModel.taskDescriptionState = ""
        viewModel.taskAddressState = ""
        viewModel.taskDeadline = ""
        viewModel.taskPriority = "4"
    }

    // Reset the "has changed" flag since we just loaded initial values
    viewModel.taskHasBeenChanged = false

    ModalBottomSheet(
        onDismissRequest = {
            // Handle tap-outside-to-dismiss with same logic as back button
            if (viewModel.taskHasBeenChanged && !allowDismiss.value) {
                openConfirmDialog.value = true // Show confirmation if there are unsaved changes
            } else {
                onDismiss() // Allow immediate dismissal if no changes or dismissal is allowed
            }
        },
        sheetState = sheetState,
        dragHandle = { /* Empty so there is no drag handle*/ },
        modifier = Modifier.padding(0.dp)
    ) {
        // Intercept back press only while the sheet is visible
        BackHandler(enabled = sheetState.isVisible) {
            if (viewModel.taskHasBeenChanged && !allowDismiss.value) {
                openConfirmDialog.value = true
            } else {
                onDismiss()
            }
        }

        // Common text style for input fields
        val textStyle = TextStyle(
            fontSize = 20.sp,
            color = Color.Black
        )

        Column(modifier = Modifier.padding(6.dp)) {

            // Task title input field
            TextField(
                singleLine = true,
                value = viewModel.taskTitleState,
                onValueChange = { newValue ->
                    viewModel.onTaskTitleChanged(newValue)
                },
                textStyle = textStyle,
                placeholder = { Text("Task Title", style = TextStyle(fontSize = 20.sp, color = Color.Gray)) },
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = colorResource(id = R.color.nice_blue),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent
                ),
                modifier = Modifier.focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    //keyboardController?.hide()
                })
            )

            // Task description input field
            TextField(
                value = viewModel.taskDescriptionState,
                onValueChange = { newValue ->
                    viewModel.onTaskDescriptionChanged(newValue)
                },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                placeholder = { Text("Description", style = TextStyle(fontSize = 16.sp, color = Color.Gray)) },
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = colorResource(id = R.color.nice_blue),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent
                )
            )

            // Additional task options (priority, deadline, etc.)
            ScrollableRow(viewModel)

            Spacer(modifier = Modifier.height(6.dp))

            // Submit button section
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Determine button state based on form validation
                var isValid = false
                var buttonColor: Color
                if (viewModel.taskTitleState.isNotBlank()) {
                    buttonColor = colorResource(id = R.color.nice_blue)
                    isValid = true
                } else {
                    buttonColor = Color.Gray
                }

                // Submit button - always enabled but ignores clicks when invalid
                Button(
                    onClick = {
                        if (!isValid) return@Button // ignore click if not valid

                        // Create task object based on whether we're editing or creating
                        if (id == 0L) {
                            // Creating new task
                            onSubmit(
                                Task(
                                    title = viewModel.taskTitleState,
                                    description = viewModel.taskDescriptionState,
                                    address = viewModel.taskAddressState,
                                    priority = viewModel.taskPriority,
                                    deadline = viewModel.taskDeadline
                                )
                            )
                        } else {
                            // Updating existing task
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
                    enabled = true, // always enabled so appearance never changes
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = buttonColor
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }

            }

        }

        // Confirmation dialog for unsaved changes
        // Shows when user tries to dismiss with unsaved changes via any method
        if (openConfirmDialog.value) {
            Dialog(onDismissRequest = { openConfirmDialog.value = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000)), // Semi-transparent backdrop
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 320.dp)
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dialog title
                        Text(
                            text = "Discard changes?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )
                        // Dialog message
                        Text(
                            text = "Your changes will be lost.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            lineHeight = 20.sp
                        )
                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            // Cancel button - closes dialog but keeps the sheet open
                            TextButton(
                                onClick = {
                                    openConfirmDialog.value = false
                                    allowDismiss.value = false // Ensure dismissal remains blocked
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = colorResource(id = R.color.nice_blue),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // Discard button - allows dismissal and closes the sheet
                            TextButton(
                                onClick = {
                                    openConfirmDialog.value = false
                                    allowDismiss.value = true // Allow dismissal to proceed
                                    onDismiss() // Actually dismiss the sheet
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Discard",
                                    color = colorResource(id = R.color.nice_blue),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

