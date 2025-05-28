package com.example.todolist

import android.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource


object PriorityUtils {

    // You can store resource IDs here
    val priorityColorRes = mapOf(
        1 to R.color.priority_red,
        2 to R.color.priority_orange,
        3 to R.color.priority_blue,
    )

    val priorityIcons = mapOf(
        1 to Icons.Default.PriorityHigh,
        2 to Icons.Default.Warning,
        3 to Icons.Default.Info,
        4 to Icons.Default.Help,
    )

    // Provide default values
    val defaultColorRes = R.color.light_gray
    val defaultIcon = Icons.Default.Label

    // Composable helper to get Color by priority
    @Composable
    fun getColor(priority: Int): androidx.compose.ui.graphics.Color {
        val resId = priorityColorRes[priority] ?: defaultColorRes
        return colorResource(id = resId)
    }

    // Non-composable helper to get Icon by priority
    fun getIcon(priority: Int): ImageVector {
        return priorityIcons[priority] ?: defaultIcon
    }
}
