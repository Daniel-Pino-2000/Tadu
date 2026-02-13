package com.myapp.tadu.data

data class UiState(
    val showBottomSheet: Boolean = false,
    val taskBeingEdited: Boolean = false,
    val showDatePicker: Boolean = false,
    val taskToUpdate: Task = Task(0, "", "", 0L, "4", ""),
    val currentId: Long = 0L
)

