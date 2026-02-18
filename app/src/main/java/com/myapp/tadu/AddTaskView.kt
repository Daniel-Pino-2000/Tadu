package com.myapp.tadu

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
import com.myapp.tadu.data.Task
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.myapp.tadu.settings.SettingsViewModel
import com.myapp.tadu.ui.theme.LocalDynamicColors
import com.myapp.tadu.view_model.TaskViewModel
import kotlinx.coroutines.launch

/**
 * A composable that displays a modal bottom sheet for adding or editing tasks.
 *
 * Features:
 * - Confirmation dialog when dismissing with unsaved changes
 * - Handles back button, swipe-to-dismiss, and tap-outside-to-dismiss consistently
 * - Auto-focuses title field and shows keyboard when opened
 * - Validates input and disables submit for invalid tasks
 * - Properly handles reminder data collection and scheduling
 * - Animates sheet closed before notifying parent (prevents abrupt removal)
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
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    onSubmit: (task: Task) -> Unit,
    isHistoryMode: Boolean = false,
    onDelete: ((Long) -> Unit)? = null
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for controlling the confirmation dialog visibility
    val openConfirmDialog = remember { mutableStateOf(false) }
    val openDeleteDialog = remember { mutableStateOf(false) }

    // Flag to control when dismissal should be allowed (bypasses confirmation)
    val allowDismiss = remember { mutableStateOf(false) }

    // Reminder state management
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var reminderText by remember { mutableStateOf<String?>(null) }

    // We can't reference sheetState inside its own initializer, so we use a flag
    // to trigger a re-expand via LaunchedEffect after confirmValueChange blocks a hide.
    val snapSheetOpen = remember { mutableStateOf(false) }

    val sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            if (newValue == SheetValue.Hidden && viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
                snapSheetOpen.value = true  // handled by LaunchedEffect below
                openConfirmDialog.value = true
                false
            } else {
                true
            }
        }
    )

    // Re-expand the sheet when confirmValueChange blocked a hide.
    // This counteracts the partial collapse animation that may have already started.
    LaunchedEffect(snapSheetOpen.value) {
        if (snapSheetOpen.value) {
            snapSheetOpen.value = false
            sheetState.show()
        }
    }

    // ── Animate the sheet closed, THEN invoke the parent callback. ────────────
    fun animateDismiss(action: () -> Unit) {
        coroutineScope.launch {
            sheetState.hide()
            action()
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    // Focus management for automatic keyboard display
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-focus the title field and show keyboard when sheet becomes visible (only if not in history mode)
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible && !isHistoryMode && id == 0L) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Initialize form fields based on whether we're editing an existing task or creating a new one
    val task by viewModel.getTaskById(id).collectAsState(
        initial = Task(0L, "", "", System.currentTimeMillis(), "", "4", "")
    )

    LaunchedEffect(id, task) {
        if (id != 0L && task.id != 0L) {
            viewModel.populateFieldsWithTask(task)
            // ✅ Reset the changed flag AFTER populating — the user hasn't changed anything yet
            viewModel.taskHasBeenChanged = false
            reminderTime = task.reminderTime
            reminderText = if (task.reminderTime != null && task.reminderTime!! > 0) {
                task.reminderTime?.toReminderDateTime()?.let { reminderDateTime ->
                    "Set for ${reminderDateTime.date} at ${reminderDateTime.time}"
                }
            } else null
        } else if (id == 0L) {
            if (viewModel.taskDeadline.isEmpty()) {
                viewModel.resetFormFields()
            } else {
                val currentDeadline = viewModel.taskDeadline
                viewModel.resetFormFields()
                viewModel.onTaskDeadlineChanged(currentDeadline)
            }
            // ✅ Reset the changed flag AFTER resetting fields — the user hasn't changed anything yet
            viewModel.taskHasBeenChanged = false
            reminderTime = null
            reminderText = null
        }
    }

    // ✅ REMOVED: `viewModel.taskHasBeenChanged = false` that was here directly in the
    // composable body. It was running on EVERY recomposition, continuously resetting
    // the flag and making the "unsaved changes" check never trigger.

    ModalBottomSheet(
        windowInsets = WindowInsets(0),
        onDismissRequest = {
            // onDismissRequest fires AFTER ModalBottomSheet has already completed
            // its own hide animation (swipe-to-dismiss or tap-outside), so we call
            // the parent callback directly here — no animateDismiss needed.
            //
            // NOTE: confirmValueChange above will intercept swipe/tap-outside BEFORE
            // the sheet hides when taskHasBeenChanged is true, so by the time
            // onDismissRequest fires, either the user confirmed discard (allowDismiss=true)
            // or taskHasBeenChanged is false. The check below is a safety net.
            if (viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
                openConfirmDialog.value = true
            } else {
                onDismiss()
                viewModel.resetFormFields()
            }
        },
        sheetState = sheetState,
        dragHandle = { /* Empty so there is no drag handle */ },
        modifier = Modifier.padding(0.dp)
    ) {
        // ✅ SINGLE BackHandler — only inside the sheet content, not duplicated outside.
        // Also calls sheetState.show() to snap the sheet back if it started closing
        // before this handler fully intercepted the back press.
        BackHandler(enabled = sheetState.isVisible) {
            if (viewModel.taskHasBeenChanged && !allowDismiss.value && !isHistoryMode) {
                coroutineScope.launch { sheetState.show() }
                openConfirmDialog.value = true
            } else {
                animateDismiss {
                    onDismiss()
                    viewModel.resetFormFields()
                }
            }
        }

        // Common text style for input fields
        val textStyle = TextStyle(
            fontSize = 20.sp,
            color = Color.Black
        )

        Column(modifier = Modifier.padding(6.dp).navigationBarsPadding() ) {

            // Task title input field
            TextField(
                singleLine = true,
                value = viewModel.taskTitleState,
                onValueChange = { newValue ->
                    viewModel.onTaskTitleChanged(newValue)
                },
                textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                placeholder = {
                    Text(
                        "Task Title",
                        style = TextStyle(
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = LocalDynamicColors.current.niceColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {}),
                readOnly = isHistoryMode
            )

            // Task description input field
            TextField(
                value = viewModel.taskDescriptionState,
                onValueChange = { newValue ->
                    viewModel.onTaskDescriptionChanged(newValue)
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        "Description",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = LocalDynamicColors.current.niceColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                readOnly = isHistoryMode
            )

            // Additional task options (priority, deadline, etc.)
            ScrollableRow(viewModel, isHistoryMode)

            Spacer(modifier = Modifier.height(6.dp))

            // Reminder Section
            if (!isHistoryMode) {
                ReminderSection(
                    settingsViewModel,
                    initialReminder = reminderTime,
                    onReminderChanged = { newReminderTime, newReminderText ->
                        reminderTime = newReminderTime
                        reminderText = newReminderText
                        viewModel.taskHasBeenChanged = true
                        viewModel.onTaskReminderTimeChanged(reminderTime)
                        viewModel.onTaskReminderTextChanged(reminderText)
                    }
                )
            } else {
                if (reminderTime != null && reminderTime!! > 0) {
                    reminderText?.let { text ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Reminder was set",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            var addToCalendar by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(4.dp))

            // Submit button section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                                LocalDynamicColors.current.niceColor else Color.Gray,
                            disabledBackgroundColor = Color.Transparent,
                            disabledContentColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (hasDeadline && taskTitle.isNotEmpty())
                                LocalDynamicColors.current.niceColor.copy(alpha = 0.3f)
                            else Color.Gray.copy(alpha = 0.3f)
                        )
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
                    LocalDynamicColors.current.niceColor
                } else {
                    Color.Gray
                }

                Button(
                    onClick = {
                        if (!isValid) return@Button

                        val taskToSubmit = if (id == 0L) {
                            Task(
                                title = viewModel.taskTitleState,
                                description = viewModel.taskDescriptionState,
                                address = viewModel.taskAddressState,
                                priority = viewModel.taskPriority,
                                deadline = viewModel.taskDeadline,
                                label = viewModel.taskLabel,
                                reminderTime = reminderTime,
                                reminderText = reminderText
                            )
                        } else {
                            Task(
                                id = id,
                                title = viewModel.taskTitleState,
                                description = viewModel.taskDescriptionState,
                                date = task.date,
                                address = viewModel.taskAddressState,
                                priority = viewModel.taskPriority,
                                deadline = viewModel.taskDeadline,
                                label = viewModel.taskLabel,
                                reminderTime = reminderTime,
                                reminderText = reminderText,
                                isDeleted = task.isDeleted,
                                deletionDate = task.deletionDate,
                                isCompleted = task.isCompleted,
                                completionDate = task.completionDate
                            )
                        }

                        if (addToCalendar && !isHistoryMode) {
                            addTaskToCalendar(context, taskToSubmit.title, taskToSubmit.deadline)
                        }

                        animateDismiss { onSubmit(taskToSubmit) }
                    },
                    enabled = true,
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
                        .fillMaxSize(),
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
                        Text(
                            text = "Discard changes?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Your changes will be lost.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            lineHeight = 20.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            // Cancel — keep the sheet open
                            TextButton(
                                onClick = {
                                    openConfirmDialog.value = false
                                    allowDismiss.value = false
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = LocalDynamicColors.current.niceColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // Discard — animate sheet away, then notify parent
                            TextButton(
                                onClick = {
                                    openConfirmDialog.value = false
                                    allowDismiss.value = true
                                    animateDismiss { onDismiss() }
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Discard",
                                    color = LocalDynamicColors.current.niceColor,
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
                        .background(Color(0x80000000)),
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
                        Text(
                            text = "Delete permanently?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "This task will be permanently deleted and cannot be recovered.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            lineHeight = 20.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            // Cancel
                            TextButton(
                                onClick = {
                                    openDeleteDialog.value = false
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = LocalDynamicColors.current.niceColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            // Delete — animate sheet away, then notify parent
                            TextButton(
                                onClick = {
                                    openDeleteDialog.value = false
                                    onDelete?.invoke(id)
                                    animateDismiss { onDismiss() }
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