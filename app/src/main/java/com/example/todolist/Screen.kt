package com.example.todolist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: String, val route: String) {
    object TodayScreen: Screen("today", "home_screen")

    sealed class BottomScreen(
        val bTitle: String, val bRoute: String, val bIcon: ImageVector
    ) : Screen(bTitle, bRoute) {

        object Today: BottomScreen(bTitle = "Today", bRoute = "today", Icons.Default.CalendarToday)
        object Inbox: BottomScreen(bTitle = "Inbox", bRoute = "inbox", Icons.Default.Inbox)
        object Search: BottomScreen(bTitle = "Search", bRoute = "search", Icons.Default.Search)
    }
}

val screenInBottom = listOf(
    Screen.BottomScreen.Today,
    Screen.BottomScreen.Inbox,
    Screen.BottomScreen.Search
)