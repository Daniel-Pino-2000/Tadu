package com.example.todolist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Object to access dynamic colors easily throughout the app
 * This replaces your XML color resources
 */
object AppColors {

    /**
     * Get the current dynamic colors
     */
    @get:Composable
    val current: DynamicColors
        get() = LocalDynamicColors.current

    /**
     * Get the current accent color
     */
    @get:Composable
    val accent: Color
        get() = LocalAccentColor.current
}

/**
 * Quick access to specific colors (equivalent to your XML colors)
 */
object QuickColors {
    @get:Composable
    val niceColor: Color
        get() = LocalDynamicColors.current.niceColor

    @get:Composable
    val blueToday: Color
        get() = LocalDynamicColors.current.blueToday

    @get:Composable
    val redYesterday: Color
        get() = LocalDynamicColors.current.redYesterday

    @get:Composable
    val greenTomorrow: Color
        get() = LocalDynamicColors.current.greenTomorrow

    @get:Composable
    val lightGray: Color
        get() = LocalDynamicColors.current.lightGray

    @get:Composable
    val orange: Color
        get() = LocalDynamicColors.current.orange

    @get:Composable
    val priorityBlue: Color
        get() = LocalDynamicColors.current.priorityBlue

    @get:Composable
    val priorityRed: Color
        get() = LocalDynamicColors.current.priorityRed

    @get:Composable
    val priorityOrange: Color
        get() = LocalDynamicColors.current.priorityOrange

    @get:Composable
    val searchBarGray: Color
        get() = LocalDynamicColors.current.searchBarGray

    @get:Composable
    val dropMenuIconGray: Color
        get() = LocalDynamicColors.current.dropMenuIconGray
}