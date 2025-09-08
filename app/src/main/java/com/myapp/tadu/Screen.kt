package com.myapp.tadu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: String, val route: String) {

    object History: Screen("History", "history")
    object Calendar: Screen("Calendar", "calendar")
    object Reminders: Screen("Reminders", "reminders")
    object Settings: Screen("Settings", "settings")
    object Home: Screen("Home", "home")

    sealed class BottomScreen(
        val bTitle: String,
        val bRoute: String,
        val unselectedIcon: ImageVector,
        val selectedIcon: ImageVector
    ) : Screen(bTitle, bRoute) {

        @RequiresApi(Build.VERSION_CODES.O)
        object Today: BottomScreen(
            bTitle = "Today",
            bRoute = "today",
            unselectedIcon = CalendarTodayIcons.getOutlinedIcon(),
            selectedIcon = CalendarTodayIcons.getFilledIcon()
        )

        object Inbox: BottomScreen(
            bTitle = "Inbox",
            bRoute = "inbox",
            unselectedIcon = Icons.Outlined.Inbox,
            selectedIcon = Icons.Filled.Inbox
        )

        object Search: BottomScreen(
            bTitle = "Search",
            bRoute = "search",
            unselectedIcon = Icons.Outlined.Search,
            selectedIcon = Icons.Filled.Search
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
val screenInBottom = listOf(
    Screen.BottomScreen.Today,
    Screen.BottomScreen.Inbox,
    Screen.BottomScreen.Search
)