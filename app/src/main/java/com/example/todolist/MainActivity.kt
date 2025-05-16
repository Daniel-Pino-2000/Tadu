package com.example.todolist

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.mytodoapp.ui.theme.MyToDoAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Make the system bars (status bar and navigation bar) draw over your content
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyToDoAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize().safeContentPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Apply status bar color based on theme
                    SetStatusBarColor(color = MaterialTheme.colorScheme.background)

                    HomeView()
                }
            }
        }
    }
}

/**
 * Composable that configures the status bar color based on the background color's luminance
 * For light background colors, it sets dark status bar icons
 * For dark background colors, it sets light status bar icons
 */
@Composable
fun SetStatusBarColor(color: Color) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = true  // Determine if we should use dark icons based on background luminance

    SideEffect {
        // Set the status bar color and icon color
        systemUiController.setStatusBarColor(
            color = color,
            darkIcons = useDarkIcons
        )
    }
}