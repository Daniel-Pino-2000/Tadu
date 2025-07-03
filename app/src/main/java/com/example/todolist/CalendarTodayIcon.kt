package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import java.time.LocalDate

object CalendarTodayIcons {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getOutlinedIcon(): ImageVector {
        val currentDay = LocalDate.now().dayOfMonth
        return createCalendarOutlinedIcon(currentDay)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilledIcon(): ImageVector {
        val currentDay = LocalDate.now().dayOfMonth
        return createCalendarFilledIcon(currentDay)
    }

    private fun createCalendarOutlinedIcon(day: Int): ImageVector {
        return ImageVector.Builder(
            name = "CalendarTodayOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Calendar outline
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Main calendar body
                moveTo(3f, 6f)
                lineTo(21f, 6f)
                lineTo(21f, 20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                lineTo(4f, 21f)
                curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
                lineTo(3f, 6f)
                close()
            }

            // Top rings
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                // Left ring
                moveTo(7f, 2f)
                lineTo(7f, 6f)
                // Right ring
                moveTo(17f, 2f)
                lineTo(17f, 6f)
            }

            // Horizontal separator line
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 10f)
                lineTo(21f, 10f)
            }

            // Day number
            addDayNumber(day, false)

        }.build()
    }

    private fun createCalendarFilledIcon(day: Int): ImageVector {
        return ImageVector.Builder(
            name = "CalendarTodayFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Filled background (tintable)
            path(
                fill = SolidColor(Color.Unspecified),
                stroke = null
            ) {
                moveTo(4f, 6f)
                lineTo(20f, 6f)
                curveTo(20.55f, 6f, 21f, 6.45f, 21f, 7f)
                lineTo(21f, 20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                lineTo(4f, 21f)
                curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
                lineTo(3f, 7f)
                curveTo(3f, 6.45f, 3.45f, 6f, 4f, 6f)
                close()
            }

            // Outline stroke on top
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 6f)
                lineTo(20f, 6f)
                lineTo(20f, 20f)
                lineTo(4f, 20f)
                close()
            }

            // Top rings
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(7f, 2f)
                lineTo(7f, 6f)
                moveTo(17f, 2f)
                lineTo(17f, 6f)
            }

            // Horizontal separator
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 10f)
                lineTo(21f, 10f)
            }

            // Day number (always white)
            addDayNumber(day, true)

        }.build()
    }



    private fun ImageVector.Builder.addDayNumber(day: Int, isOnFilledBackground: Boolean) {
        val dayString = day.toString()
        // Use white color for filled background, black for outlined
        val color = if (isOnFilledBackground) Color.White else Color.Black

        // For single digit days
        if (day < 10) {
            when (day) {
                1 -> addDigit1(12f, 15.5f, color)
                2 -> addDigit2(12f, 15.5f, color)
                3 -> addDigit3(12f, 15.5f, color)
                4 -> addDigit4(12f, 15.5f, color)
                5 -> addDigit5(12f, 15.5f, color)
                6 -> addDigit6(12f, 15.5f, color)
                7 -> addDigit7(12f, 15.5f, color)
                8 -> addDigit8(12f, 15.5f, color)
                9 -> addDigit9(12f, 15.5f, color)
            }
        } else {
            // For two digit days
            val firstDigit = day / 10
            val secondDigit = day % 10

            // First digit positioned to the left
            when (firstDigit) {
                1 -> addDigit1(9f, 15.5f, color)
                2 -> addDigit2(9f, 15.5f, color)
                3 -> addDigit3(9f, 15.5f, color)
            }

            // Second digit positioned to the right
            when (secondDigit) {
                0 -> addDigit0(15f, 15.5f, color)
                1 -> addDigit1(15f, 15.5f, color)
                2 -> addDigit2(15f, 15.5f, color)
                3 -> addDigit3(15f, 15.5f, color)
                4 -> addDigit4(15f, 15.5f, color)
                5 -> addDigit5(15f, 15.5f, color)
                6 -> addDigit6(15f, 15.5f, color)
                7 -> addDigit7(15f, 15.5f, color)
                8 -> addDigit8(15f, 15.5f, color)
                9 -> addDigit9(15f, 15.5f, color)
            }
        }
    }

    private fun ImageVector.Builder.addDigit0(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 2f, y - 2.5f)
            lineTo(x + 2f, y - 2.5f)
            lineTo(x + 2f, y + 2.5f)
            lineTo(x - 2f, y + 2.5f)
            close()
            moveTo(x - 1.5f, y - 2f)
            lineTo(x - 1.5f, y + 2f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x + 1.5f, y - 2f)
            close()
        }
    }


    private fun ImageVector.Builder.addDigit1(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 0.7f, y - 2.5f)
            lineTo(x + 0.7f, y - 2.5f)
            lineTo(x + 0.7f, y + 2.5f)
            lineTo(x - 0.7f, y + 2.5f)
            close()
        }
    }


    private fun ImageVector.Builder.addDigit2(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 0.5f)
            lineTo(x - 1.5f, y - 0.5f)
            lineTo(x - 1.5f, y + 0.5f)
            lineTo(x + 1.5f, y + 0.5f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 2f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit3(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 1.5f)
            lineTo(x + 1f, y + 1.5f)
            lineTo(x + 1f, y + 0.5f)
            lineTo(x - 1f, y + 0.5f)
            lineTo(x - 1f, y - 0.5f)
            lineTo(x + 1f, y - 0.5f)
            lineTo(x + 1f, y - 1.5f)
            lineTo(x - 1.5f, y - 1.5f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit4(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x - 1f, y - 2f)
            lineTo(x - 1f, y)
            lineTo(x + 1.5f, y)
            lineTo(x + 1.5f, y + 0.5f)
            lineTo(x + 1f, y + 0.5f)
            lineTo(x + 1f, y + 2f)
            lineTo(x + 0.5f, y + 2f)
            lineTo(x + 0.5f, y + 0.5f)
            lineTo(x - 1.5f, y + 0.5f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit5(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 1.5f)
            lineTo(x - 1f, y - 1.5f)
            lineTo(x - 1f, y - 0.5f)
            lineTo(x + 1.5f, y - 0.5f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 2f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit6(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 1.5f)
            lineTo(x - 1f, y - 1.5f)
            lineTo(x - 1f, y - 0.5f)
            lineTo(x + 1.5f, y - 0.5f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 2f)
            close()
            moveTo(x - 1f, y)
            lineTo(x + 1f, y)
            lineTo(x + 1f, y + 1.5f)
            lineTo(x - 1f, y + 1.5f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit7(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x + 1f, y + 2f)
            lineTo(x + 1f, y - 1.5f)
            lineTo(x - 1.5f, y - 1.5f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit8(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 2f)
            close()
            moveTo(x - 1f, y - 1.5f)
            lineTo(x - 1f, y - 0.5f)
            lineTo(x + 1f, y - 0.5f)
            lineTo(x + 1f, y - 1.5f)
            close()
            moveTo(x - 1f, y)
            lineTo(x - 1f, y + 1.5f)
            lineTo(x + 1f, y + 1.5f)
            lineTo(x + 1f, y)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit9(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            moveTo(x - 1.5f, y - 2f)
            lineTo(x + 1.5f, y - 2f)
            lineTo(x + 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 2f)
            lineTo(x - 1.5f, y + 1.5f)
            lineTo(x + 1f, y + 1.5f)
            lineTo(x + 1f, y)
            lineTo(x - 1f, y)
            lineTo(x - 1f, y - 1.5f)
            lineTo(x + 1f, y - 1.5f)
            lineTo(x + 1f, y - 0.5f)
            lineTo(x - 1.5f, y - 0.5f)
            close()
        }
    }
}