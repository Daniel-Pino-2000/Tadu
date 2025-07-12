package com.example.todolist

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
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
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.MaterialTheme


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
    onSubmit: (task: Task) -> Unit,
    isHistoryMode: Boolean = false, // New parameter to indicate if we're in history mode
    onDelete: ((Long) -> Unit)? = null // Callback for delete action
) {

    val context = LocalContext.current

    // State for controlling the confirmation dialog visibility
    val openConfirmDialog = remember { mutableStateOf(false) }
    val openDeleteDialog = remember { mutableStateOf(false) } // New delete confirmation dialog

    // Flag to control when dismissal should be allowed (bypasses confirmation)
    val allowDismiss = remember { mutableStateOf(false) }

    // Configure the bottom sheet state with custom dismiss behavior
    var sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // When trying to hide the sheet, check if there are unsaved changes
            if (newValue == SheetValue.Hidden && viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
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
        if (viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
            openConfirmDialog.value = true // Show confirmation if there are unsaved changes
        } else {
            onDismiss() // Allow immediate dismissal if no changes or dismissal is allowed
        }
    }

    // Auto-focus the title field and show keyboard when sheet becomes visible (only if not in history mode)
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible && !isHistoryMode) {
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
        viewModel.taskLabel = task.value.label
    }
    else {
        // Creating new task - reset fields to defaults
        viewModel.taskTitleState = ""
        viewModel.taskDescriptionState = ""
        viewModel.taskAddressState = ""
        viewModel.taskDeadline = ""
        viewModel.taskPriority = ""
        viewModel.taskLabel = ""
    }

    // Reset the "has changed" flag since we just loaded initial values
    viewModel.taskHasBeenChanged = false

    ModalBottomSheet(
        onDismissRequest = {
            // Handle tap-outside-to-dismiss with same logic as back button
            if (viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
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
            if (viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
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
                }),
                readOnly = isHistoryMode // Make read-only in history mode
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
                ),
                readOnly = isHistoryMode // Make read-only in history mode
            )

            // Additional task options (priority, deadline, etc.) - only show if not in history mode
            //if (!isHistoryMode) {
                ScrollableRow(viewModel, isHistoryMode)
            //}

            Spacer(modifier = Modifier.height(6.dp))

            var addToCalendar by remember { mutableStateOf(false) } // Checkbox state

            // Submit button section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = if (isHistoryMode) Arrangement.SpaceBetween else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (isHistoryMode) {
                    // Delete button for history mode
                    Button(
                        onClick = {
                            openDeleteDialog.value = true
                        },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.error,
                            disabledBackgroundColor = Color.Transparent,
                            disabledContentColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Permanently",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Calendar button for normal mode
                    val hasDeadline = viewModel.taskDeadline.isNotBlank()
                    val taskTitle = viewModel.taskTitleState
                    val taskDeadline = viewModel.taskDeadline

                    Button(
                        onClick = {
                            addTaskToCalendar(context, taskTitle, taskDeadline)
                        },
                        enabled = hasDeadline && taskTitle.isNotEmpty(),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = if (hasDeadline && taskTitle.isNotEmpty())
                                colorResource(id = R.color.nice_blue) else Color.Gray,
                            disabledBackgroundColor = Color.Transparent,
                            disabledContentColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Add to Calendar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add to Calendar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Submit/Restore button
                val isValid = viewModel.taskTitleState.isNotBlank()
                val buttonColor = if (isValid) {
                    colorResource(id = R.color.nice_blue)
                } else {
                    Color.Gray
                }

                Button(
                        onClick = {
                            if (!isValid) return@Button // ignore click if not valid


                                // Create task object based on whether we're editing or creating
                                val task = if (id == 0L) {
                                    Task(
                                        title = viewModel.taskTitleState,
                                        description = viewModel.taskDescriptionState,
                                        address = viewModel.taskAddressState,
                                        priority = viewModel.taskPriority,
                                        deadline = viewModel.taskDeadline,
                                        label = viewModel.taskLabel
                                    )
                                } else {
                                    Task(
                                        id = id,
                                        title = viewModel.taskTitleState,
                                        description = viewModel.taskDescriptionState,
                                        date = viewModel.taskDateState,
                                        address = viewModel.taskAddressState,
                                        priority = viewModel.taskPriority,
                                        deadline = viewModel.taskDeadline,
                                        label = viewModel.taskLabel
                                    )
                                }

                                // Submit the task
                                onSubmit(task)


                                // Add to calendar if checked and not in history mode
                                if (addToCalendar && !isHistoryMode) {
                                    addTaskToCalendar(context = context, task.title, task.deadline)
                                }


                        },

                    enabled = true, // always enabled so appearance never changes
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = buttonColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isHistoryMode) Icons.Default.Restore else Icons.Default.Check,
                        contentDescription = if (isHistoryMode) "Restore Task" else "Submit Task",
                        tint = Color.White
                    )
                }
            }

        }

        // Confirmation dialog for unsaved changes (only in normal mode)
        if (openConfirmDialog.value && !isHistoryMode) {
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

        // Delete confirmation dialog for history mode
        if (openDeleteDialog.value && isHistoryMode) {
            Dialog(onDismissRequest = { openDeleteDialog.value = false }) {
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
                            text = "Delete permanently?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )
                        // Dialog message
                        Text(
                            text = "This task will be permanently deleted and cannot be recovered.",
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
                            // Cancel button
                            TextButton(
                                onClick = {
                                    openDeleteDialog.value = false
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
                            // Delete button
                            TextButton(
                                onClick = {
                                    openDeleteDialog.value = false
                                    onDelete?.invoke(id)
                                    onDismiss()
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Delete",
                                    color = MaterialTheme.colorScheme.error,
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