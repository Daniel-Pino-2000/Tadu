package com.example.todolist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: String, val route: String) {
    object TodayScreen: Screen("today", "today")

    sealed class BottomScreen(
        val bTitle: String, val bRoute: String, val unselectedIcon: ImageVector, val selectedIcon: ImageVector
    ) : Screen(bTitle, bRoute) {

        object Today: BottomScreen(bTitle = "Today", bRoute = "today", Icons.Outlined.CalendarToday, Icons.Filled.CalendarToday)
        object Inbox: BottomScreen(bTitle = "Inbox", bRoute = "inbox", Icons.Outlined.Inbox, Icons.Filled.Inbox)
        object Search: BottomScreen(bTitle = "Search", bRoute = "search", Icons.Outlined.Search, Icons.Filled.Search)
    }
}

val screenInBottom = listOf(
    Screen.BottomScreen.Today,
    Screen.BottomScreen.Inbox,
    Screen.BottomScreen.Search
)