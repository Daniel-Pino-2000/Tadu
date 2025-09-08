package com.myapp.tadu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
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
                moveTo(3f, 6f)
                lineTo(21f, 6f)
                lineTo(21f, 20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                lineTo(4f, 21f)
                curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
                lineTo(3f, 6f)
                close()
            }

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

            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(3f, 10f)
                lineTo(21f, 10f)
            }

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

            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(4f, 10f)
                lineTo(20f, 10f)
            }

            addDayNumber(day, true)
        }.build()
    }

    private fun ImageVector.Builder.addDayNumber(day: Int, isOnFilledBackground: Boolean) {
        val color = if (isOnFilledBackground) Color.White else Color.Black
        val singleDigitY = 14.5f
        val firstDigitX = 9f
        val secondDigitX = 15f
        val twoDigitY = 14.5f

        if (day < 10) {
            when (day) {
                1 -> addDigit1(12f, singleDigitY, color)
                2 -> addDigit2(12f, singleDigitY, color)
                3 -> addDigit3(12f, singleDigitY, color)
                4 -> addDigit4(12f, singleDigitY, color)
                5 -> addDigit5(12f, singleDigitY, color)
                6 -> addDigit6(12f, singleDigitY, color)
                7 -> addDigit7(12f, singleDigitY, color)
                8 -> addDigit8(12f, singleDigitY, color)
                9 -> addDigit9(12f, singleDigitY, color)
            }
        } else {
            val firstDigit = day / 10
            val secondDigit = day % 10

            when (firstDigit) {
                1 -> addDigit1(firstDigitX, twoDigitY, color)
                2 -> addDigit2(firstDigitX, twoDigitY, color)
                3 -> addDigit3(firstDigitX, twoDigitY, color)
            }

            when (secondDigit) {
                0 -> addDigit0(secondDigitX, twoDigitY, color)
                1 -> addDigit1(secondDigitX, twoDigitY, color)
                2 -> addDigit2(secondDigitX, twoDigitY, color)
                3 -> addDigit3(secondDigitX, twoDigitY, color)
                4 -> addDigit4(secondDigitX, twoDigitY, color)
                5 -> addDigit5(secondDigitX, twoDigitY, color)
                6 -> addDigit6(secondDigitX, twoDigitY, color)
                7 -> addDigit7(secondDigitX, twoDigitY, color)
                8 -> addDigit8(secondDigitX, twoDigitY, color)
                9 -> addDigit9(secondDigitX, twoDigitY, color)
            }
        }
    }

    private fun ImageVector.Builder.addDigit0(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Outer rectangle
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
            // Inner rectangle (hole)
            moveTo(x - 1.2f, y - 1.8f)
            lineTo(x - 1.2f, y + 1.8f)
            lineTo(x + 1.2f, y + 1.8f)
            lineTo(x + 1.2f, y - 1.8f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit1(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Vertical line
            moveTo(x - 0.3f, y - 2.4f)
            lineTo(x + 0.3f, y - 2.4f)
            lineTo(x + 0.3f, y + 2.4f)
            lineTo(x - 0.3f, y + 2.4f)
            close()
            // Top diagonal
            moveTo(x - 0.9f, y - 1.8f)
            lineTo(x - 0.3f, y - 2.4f)
            lineTo(x + 0.3f, y - 2.4f)
            lineTo(x - 0.3f, y - 1.8f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit2(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Top horizontal
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x - 1.8f, y - 1.8f)
            close()
            // Top right vertical
            moveTo(x + 1.2f, y - 1.8f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.2f, y - 0.3f)
            close()
            // Middle horizontal
            moveTo(x - 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x - 1.8f, y + 0.3f)
            close()
            // Bottom left vertical
            moveTo(x - 1.8f, y + 0.3f)
            lineTo(x - 1.2f, y + 0.3f)
            lineTo(x - 1.2f, y + 1.8f)
            lineTo(x - 1.8f, y + 1.8f)
            close()
            // Bottom horizontal
            moveTo(x - 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit3(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Top horizontal
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x - 1.8f, y - 1.8f)
            close()
            // Top right vertical
            moveTo(x + 1.2f, y - 1.8f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.2f, y - 0.3f)
            close()
            // Middle horizontal
            moveTo(x - 1.2f, y - 0.3f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x - 1.2f, y + 0.3f)
            close()
            // Bottom right vertical
            moveTo(x + 1.2f, y + 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.2f, y + 1.8f)
            close()
            // Bottom horizontal
            moveTo(x - 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit4(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Left vertical (top part)
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x - 1.2f, y - 2.4f)
            lineTo(x - 1.2f, y - 0.3f)
            lineTo(x - 1.8f, y - 0.3f)
            close()
            // Right vertical (full height)
            moveTo(x + 1.2f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x + 1.2f, y + 2.4f)
            close()
            // Middle horizontal
            moveTo(x - 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x - 1.8f, y + 0.3f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit5(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Top horizontal
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x - 1.8f, y - 1.8f)
            close()
            // Top left vertical
            moveTo(x - 1.8f, y - 1.8f)
            lineTo(x - 1.2f, y - 1.8f)
            lineTo(x - 1.2f, y - 0.3f)
            lineTo(x - 1.8f, y - 0.3f)
            close()
            // Middle horizontal
            moveTo(x - 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x - 1.8f, y + 0.3f)
            close()
            // Bottom right vertical
            moveTo(x + 1.2f, y + 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.2f, y + 1.8f)
            close()
            // Bottom horizontal
            moveTo(x - 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit6(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Top horizontal
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x - 1.8f, y - 1.8f)
            close()
            // Left vertical (full height)
            moveTo(x - 1.8f, y - 1.8f)
            lineTo(x - 1.2f, y - 1.8f)
            lineTo(x - 1.2f, y + 1.8f)
            lineTo(x - 1.8f, y + 1.8f)
            close()
            // Middle horizontal
            moveTo(x - 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x - 1.8f, y + 0.3f)
            close()
            // Bottom right vertical
            moveTo(x + 1.2f, y + 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.2f, y + 1.8f)
            close()
            // Bottom horizontal
            moveTo(x - 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit7(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Top horizontal
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x - 1.8f, y - 1.8f)
            close()
            // Right vertical
            moveTo(x + 1.2f, y - 1.8f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x + 1.2f, y + 2.4f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit8(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Outer rectangle
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
            // Top inner rectangle (hole)
            moveTo(x - 1.2f, y - 1.8f)
            lineTo(x - 1.2f, y - 0.3f)
            lineTo(x + 1.2f, y - 0.3f)
            lineTo(x + 1.2f, y - 1.8f)
            close()
            // Bottom inner rectangle (hole)
            moveTo(x - 1.2f, y + 0.3f)
            lineTo(x - 1.2f, y + 1.8f)
            lineTo(x + 1.2f, y + 1.8f)
            lineTo(x + 1.2f, y + 0.3f)
            close()
        }
    }

    private fun ImageVector.Builder.addDigit9(x: Float, y: Float, color: Color) {
        path(fill = SolidColor(color)) {
            // Top horizontal
            moveTo(x - 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 2.4f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x - 1.8f, y - 1.8f)
            close()
            // Top left vertical
            moveTo(x - 1.8f, y - 1.8f)
            lineTo(x - 1.2f, y - 1.8f)
            lineTo(x - 1.2f, y - 0.3f)
            lineTo(x - 1.8f, y - 0.3f)
            close()
            // Right vertical (full height)
            moveTo(x + 1.2f, y - 1.8f)
            lineTo(x + 1.8f, y - 1.8f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.2f, y + 1.8f)
            close()
            // Middle horizontal
            moveTo(x - 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y - 0.3f)
            lineTo(x + 1.8f, y + 0.3f)
            lineTo(x - 1.8f, y + 0.3f)
            close()
            // Bottom horizontal
            moveTo(x - 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 1.8f)
            lineTo(x + 1.8f, y + 2.4f)
            lineTo(x - 1.8f, y + 2.4f)
            close()
        }
    }
}