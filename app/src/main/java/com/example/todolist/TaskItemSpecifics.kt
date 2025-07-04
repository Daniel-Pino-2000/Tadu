package com.example.todolist

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(task: Task, viewModel: TaskViewModel, onClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }

    val elevationValue = 8.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
            .clickable { onClick() },
        backgroundColor = colorResource(id = R.color.light_gray),
        shape = RoundedCornerShape(15.dp),
        elevation = elevationValue
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Spacer(modifier = Modifier.width(6.dp))

            val coroutineScope = rememberCoroutineScope()

            val priority: Int = if (task.priority.isNotEmpty()) {
                task.priority.toInt()
            } else {
                4
            }


            CircularCheckbox(

                checked = isChecked,
                priority = priority,
                onCheckedChange = { checked ->
                    isChecked = checked
                    if (checked) {
                        coroutineScope.launch {
                            delay(350)
                            viewModel.completeTask(task.id)
                        }

                    }
                }
            )


            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Light)
                )


                Spacer(modifier = Modifier.height(4.dp))

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.body2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (task.deadline.isNotEmpty()) {
                        DeadlineItem(task)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (task.address.isNotEmpty()) {
                        val context = LocalContext.current
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Has address",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    openAddressInMaps(
                                        context = context,
                                        viewModel.taskAddressState
                                    )
                                }
                                .align(Alignment.CenterVertically),
                            tint = Color.Black,
                        )
                    }


                }


            }
        }


    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeadlineItem(task: Task) {
    // Format the date for the comparison
    val today = LocalDate.now()
    val currentYear = today.year
    val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)

    val yesterday = today.minusDays(1)
    val tomorrow = today.plusDays(1)

    // Parse the selectedDate string to LocalDate by appending current year
    val deadlineText = task.deadline.trim().replaceFirstChar { it.uppercaseChar() }
    val parsedDate = LocalDate.parse("$deadlineText $currentYear", formatter)

    // Check the different possible dates
    val dateStatus: String = when {
        parsedDate.isEqual(today) -> "Today"
        parsedDate.isEqual(yesterday) -> "Yesterday"
        parsedDate.isBefore(today) -> "Past"
        parsedDate.isEqual(tomorrow) -> "Tomorrow"
        else -> "Future"
    }

    // Select the color depending on the result of the comparison
    val iconColor = when (dateStatus) {

        "Today" -> colorResource(id = R.color.blue_today)
        "Past"  -> colorResource(id = R.color.red_yesterday)
        "Yesterday" -> colorResource(id = R.color.red_yesterday)
        "Future" -> colorResource(id = R.color.green_tomorrow)
        "Tomorrow" -> colorResource(id = R.color.green_tomorrow)
        else -> Color.Gray
    }

    // Assign the text that will be displayed
    val dateText = if (dateStatus == "Today" || dateStatus == "Yesterday" || dateStatus == "Tomorrow") {
        dateStatus
    } else {
        task.deadline
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.padding(end = 2.dp))


        Text(
            text = dateText,
            color = iconColor,
            style = MaterialTheme.typography.body2
        )
    }
}

// This wont be used for now
@Composable
fun AddressItem(task: Task, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = task.address,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.body2
        )
    }
}


@Composable
fun CircularCheckbox(
    checked: Boolean,
    priority: Int = 4,
    onCheckedChange: (Boolean) -> Unit,

    ) {
    val size: Dp = 23.dp
    val checkedColor: Color = colorResource(id = R.color.nice_blue)
    val checkmarkColor: Color = Color.White

    val borderColor: Color = if(priority < 4) {
        PriorityUtils.getBorderColor(priority)
    } else {
        Color.Gray
    }

    // Track uncheckedColor reactively based on priority
    var uncheckedColor = PriorityUtils.getColor(priority)

    var border: Dp = if(priority < 4) {
        2.dp
    } else {
        1.dp
    }


    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (checked) borderColor else uncheckedColor)
            .border(border, borderColor, CircleShape) // Add border
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = checkmarkColor,
                modifier = Modifier.size(size * 1f)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePicker(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            val formatter = DateTimeFormatter.ofPattern("MMM dd")
            val selectedDate = selectedLocalDate.format(formatter)
            onDateSelected(selectedDate)
        },
        year, month, day
    ).show()
}

fun openAddressInMaps(context: Context, address: String) {
    try {
        val encodedLocation = Uri.encode(address.trim())
        val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            context.startActivity(fallbackIntent)
        }
    } catch (e: Exception) {
        Log.e("MapsLaunch", "Failed to open maps", e)
        Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
    }
}