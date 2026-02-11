package com.myapp.tadu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myapp.tadu.data.Task
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.sin
// Import for Lottie animations
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.myapp.tadu.ui.theme.LocalDynamicColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.myapp.tadu.view_model.TaskViewModel
import kotlinx.coroutines.CoroutineScope


@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNavScreens(
    viewModel: TaskViewModel,
    currentRoute: String,
    modifier: Modifier = Modifier,
    undoToastManager: UndoToastManager
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedLabel by rememberSaveable { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }

    // Memorize date calculations
    val dateInfo = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH)
        val niceFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)
        DateInfo(
            today = today,
            formatter = formatter,
            niceFormatter = niceFormatter,
            currentYear = today.year,
            nicerDate = today.format(niceFormatter)
        )
    }

    val listState = rememberLazyListState()

    // Scroll to top on screen change
    LaunchedEffect(currentRoute) {
        listState.scrollToItem(0)
    }

    val taskList = viewModel.getPendingTasks.collectAsState(initial = listOf())
    val tasks = taskList.value

    // Observe UI state for keyboard management
    val uiState by viewModel.uiState.collectAsState()

    // Handle keyboard management with smoother transitions
    LaunchedEffect(uiState.showBottomSheet, uiState.taskBeingEdited) {
        if (!uiState.showBottomSheet && !uiState.taskBeingEdited) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    // Memoize filtered tasks to avoid recomputation
    val tasksToDisplay = remember(tasks, currentRoute, searchQuery, selectedLabel, dateInfo) {
        filterTasks(tasks, currentRoute, searchQuery, selectedLabel, dateInfo)
    }

    // Get colors outside of remember block for consistency
    val niceColor = LocalDynamicColors.current.niceColor

    // Memoize grouped tasks with better performance
    val groupedTasks = remember(tasksToDisplay, dateInfo, currentRoute, searchQuery, selectedLabel, niceColor) {
        groupTasks(tasksToDisplay, currentRoute, searchQuery, selectedLabel, dateInfo, niceColor)
    }

    // Memoize label data
    val labelData = remember(tasks) {
        tasks.asSequence()
            .mapNotNull { task -> if (task.label.isNotBlank()) task.label else null }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedBy { it.first }
    }

    // Clear state when switching routes with smooth transition
    LaunchedEffect(currentRoute) {
        if (currentRoute != "search") {
            keyboardController?.hide()
            focusManager.clearFocus()
            searchQuery = ""
            selectedLabel = null
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search section - only show when current route is "search"
        if (currentRoute == "search") {
            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    if (query.isNotBlank()) {
                        selectedLabel = null
                    }
                },
                selectedLabel = selectedLabel,
                onLabelSelectionChange = { label ->
                    selectedLabel = label
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                onClearSelection = { selectedLabel = null },
                labelData = labelData,
                niceColor = niceColor,
                searchFocusRequester = searchFocusRequester,
                keyboardController = keyboardController,
                focusManager = focusManager
            )
        }

        // Task list with optimized rendering
        TaskList(
            currentRoute = currentRoute,
            searchQuery = searchQuery,
            selectedLabel = selectedLabel,
            groupedTasks = groupedTasks,
            labelData = labelData,
            viewModel = viewModel,
            undoToastManager = undoToastManager,
            keyboardController = keyboardController,
            focusManager = focusManager,
            listState
        )
    }
}

// Data class for date information
private data class DateInfo(
    val today: LocalDate,
    val formatter: DateTimeFormatter,
    val niceFormatter: DateTimeFormatter,
    val currentYear: Int,
    val nicerDate: String
)

// Data class for task groups
private data class TaskGroup(
    val title: String,
    val tasks: List<Task>,
    val color: Color
)

// Separate function for filtering tasks - more efficient
@RequiresApi(Build.VERSION_CODES.O)
private fun filterTasks(
    tasks: List<Task>,
    currentRoute: String,
    searchQuery: String,
    selectedLabel: String?,
    dateInfo: DateInfo
): List<Task> {

    val formatterWithYear = dateInfo.formatter // "MMM dd yyyy"
    val formatterWithoutYear = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)

    fun parseTaskDate(deadline: String): LocalDate? {
        val trimmed = deadline.trim()
        return runCatching { LocalDate.parse(trimmed, formatterWithYear) }.getOrElse {
            runCatching {
                val legacy = LocalDate.parse(trimmed, formatterWithoutYear)
                legacy.withYear(dateInfo.currentYear)
            }.getOrNull()
        }
    }

    return when (currentRoute) {
        "today" -> tasks.filter { task ->
            val taskDate = parseTaskDate(task.deadline)
            taskDate != null && !taskDate.isAfter(dateInfo.today) // includes overdue + today
        }


        "search" -> when {
            searchQuery.isNotBlank() -> tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
            selectedLabel != null -> tasks.filter { it.label.equals(selectedLabel, ignoreCase = true) }
            else -> emptyList()
        }
        "inbox" -> tasks
        else -> tasks
    }
}


// Separate function for grouping tasks - more efficient
@RequiresApi(Build.VERSION_CODES.O)
private fun groupTasks(
    tasksToDisplay: List<Task>,
    currentRoute: String,
    searchQuery: String,
    selectedLabel: String?,
    dateInfo: DateInfo,
    niceBlueColor: Color
): List<TaskGroup> {
    return when {
        currentRoute == "search" -> {
            val sortedTasks = tasksToDisplay.sortedBy { task ->
                (if (task.priority.isBlank()) "4" else task.priority).toInt()
            }
            if (sortedTasks.isNotEmpty()) {
                val title = when {
                    searchQuery.isNotBlank() -> "Search Results"
                    selectedLabel != null -> "Tasks with \"$selectedLabel\""
                    else -> "Tasks"
                }
                listOf(TaskGroup(title, sortedTasks, niceBlueColor))
            } else {
                emptyList()
            }
        }
        currentRoute == "inbox" -> {
            if (tasksToDisplay.isNotEmpty()) {
                val sortedTasks = tasksToDisplay.sortedBy { it.date }
                listOf(TaskGroup("All Tasks", sortedTasks, Color.Gray))
            } else {
                emptyList()
            }
        }
        else -> {
            val (overdueTasks, todayTasks, futureTasks) = categorizeTasksByDate(tasksToDisplay, dateInfo)

            val sortByPriority: (List<Task>) -> List<Task> = { taskList ->
                taskList.sortedBy { task ->
                    (if (task.priority.isBlank()) "4" else task.priority).toInt()
                }
            }

            listOfNotNull(
                if (overdueTasks.isNotEmpty()) TaskGroup("Overdue", sortByPriority(overdueTasks), Color.Red) else null,
                if (todayTasks.isNotEmpty()) TaskGroup(dateInfo.nicerDate, sortByPriority(todayTasks), niceBlueColor) else null,
                if (futureTasks.isNotEmpty()) TaskGroup("Upcoming", sortByPriority(futureTasks), Color.Gray) else null
            )
        }
    }
}

// Helper function to categorize tasks by date
@RequiresApi(Build.VERSION_CODES.O)
private fun categorizeTasksByDate(
    tasks: List<Task>,
    dateInfo: DateInfo
): Triple<MutableList<Task>, MutableList<Task>, MutableList<Task>> {

    val overdueTasks = mutableListOf<Task>()
    val todayTasks = mutableListOf<Task>()
    val futureTasks = mutableListOf<Task>()

    // dateInfo.formatter should be "MMM dd yyyy" (Locale.ENGLISH)
    val formatterWithYear = dateInfo.formatter
    val formatterWithoutYear = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)

    tasks.forEach { task ->
        val trimmed = task.deadline.trim()

        val taskDate: LocalDate? =
            // 1) New data: already includes a year ("MMM dd yyyy")
            runCatching { LocalDate.parse(trimmed, formatterWithYear) }.getOrElse {
                // 2) Legacy data: no year ("MMM dd") -> assume current year
                runCatching {
                    val legacy = LocalDate.parse(trimmed, formatterWithoutYear)
                    legacy.withYear(dateInfo.currentYear)
                }.getOrNull()
            }

        when {
            taskDate == null -> todayTasks.add(task) // keep prior fallback behavior
            taskDate.isBefore(dateInfo.today) -> overdueTasks.add(task)
            taskDate.isEqual(dateInfo.today) -> todayTasks.add(task)
            else -> futureTasks.add(task)
        }
    }

    return Triple(overdueTasks, todayTasks, futureTasks)
}


@Composable
private fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLabel: String?,
    onLabelSelectionChange: (String?) -> Unit,
    onClearSelection: () -> Unit,
    labelData: List<Pair<String, Int>>,
    niceColor: Color,
    searchFocusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(
                text = "Search tasks...",
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // subtle adaptive text
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onSearchQueryChange("")
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .focusRequester(searchFocusRequester),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = colorScheme.surfaceContainer,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            cursorColor = colorScheme.primary
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        )
    )

    // Label selection UI
    if (selectedLabel != null) {
        SelectedLabelSection(
            selectedLabel = selectedLabel,
            onClearSelection = onClearSelection,
            niceColor = niceColor
        )
    } else if (searchQuery.isBlank()) {
        LabelFilterSection(
            labelData = labelData,
            onLabelSelected = { label ->
                onLabelSelectionChange(label)
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        )
    }
}


@Composable
private fun SelectedLabelSection(
    selectedLabel: String,
    onClearSelection: () -> Unit,
    niceColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClearSelection() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = niceColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back to labels",
                fontSize = 14.sp,
                color = niceColor,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing tasks with label:",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(niceColor.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = selectedLabel,
                    fontSize = 14.sp,
                    color = niceColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelFilterSection(
    labelData: List<Pair<String, Int>>,
    onLabelSelected: (String) -> Unit
) {
    if (labelData.isNotEmpty()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Labels",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LocalDynamicColors.current.niceColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${labelData.size} label${if (labelData.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = LocalDynamicColors.current.niceColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labelData.forEach { (label, count) ->
                    LabelChip(
                        label = label,
                        count = count,
                        onClick = { onLabelSelected(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LabelChip(
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LocalDynamicColors.current.niceColor.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = LocalDynamicColors.current.niceColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(LocalDynamicColors.current.niceColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

// ENHANCED: Lottie Animation Empty State Component with smooth loading
@Composable
private fun LottieEmptyState(
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    // Different animations and messages for different screens
    val (animationResource, mainMessage, subMessage) = when (currentRoute) {
        "today" -> Triple(
            R.raw.reading_animation, // Replace with your actual animation resource name
            "You're all caught up!",
            "All tasks done for today! Time to relax and enjoy 📖"
        )
        "inbox" -> Triple(
            R.raw.travel_is_fun_animation, // Replace with your actual animation resource name
            "Inbox Zero achieved!",
            "You've conquered all your tasks! Ready for new adventures! 🏆"
        )
        "search" -> Triple(
            R.raw.no_search_animation, // Add your search animation here
            "No tasks found",
            ""
        )
        else -> Triple(
            R.raw.default_completed, // Default animation
            "All clear!",
            "Nothing to see here. Enjoy the peace! ✨"
        )
    }

    // Load the Lottie composition
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationResource)
    )

    // Animate the composition
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f,
        restartOnPlay = false
    )

    // Smooth entrance animation that works like search
    val componentAlpha by animateFloatAsState(
        targetValue = if (composition != null) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "component_alpha"
    )

    val componentScale by animateFloatAsState(
        targetValue = if (composition != null) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "component_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .alpha(componentAlpha)
            .scale(componentScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Enhanced Lottie Animation with better loading behavior
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(y = (-20).dp),
            contentAlignment = Alignment.Center
        ) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Much cleaner placeholder - invisible until loaded
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0f) // Completely transparent
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main message with enhanced styling
        Text(
            text = mainMessage,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sub message with enhanced styling
        Text(
            text = subMessage,
            fontSize = 18.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Add some bottom spacing for better visual balance
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TaskList(
    currentRoute: String,
    searchQuery: String,
    selectedLabel: String?,
    groupedTasks: List<TaskGroup>,
    labelData: List<Pair<String, Int>>,
    viewModel: TaskViewModel,
    undoToastManager: UndoToastManager,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    listState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()

    // Get the total number of tasks to determine if any exist
    val taskList = viewModel.getPendingTasks.collectAsState(initial = listOf())
    val totalTasks = taskList.value

    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        when {
            currentRoute == "search" -> {
                when {
                    // Case 1: User has searched/selected label but no results found
                    (searchQuery.isNotBlank() || selectedLabel != null) && groupedTasks.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🔍",
                                        fontSize = 48.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    val message = when {
                                        searchQuery.isNotEmpty() -> "No tasks found for \"$searchQuery\""
                                        selectedLabel != null -> "No tasks found with label \"$selectedLabel\""
                                        else -> "No tasks found"
                                    }
                                    Text(
                                        text = message,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF616161),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Try a different search term or label",
                                        fontSize = 16.sp,
                                        color = Color(0xFF9E9E9E),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Case 2: No search query and no selected label (default search screen state)
                    searchQuery.isBlank() && selectedLabel == null -> {
                        item {
                            when {
                                // Sub-case 2a: No tasks exist at all - show search animation
                                totalTasks.isEmpty() -> {
                                    LottieEmptyState(currentRoute = currentRoute)
                                }

                                // Sub-case 2b: Tasks exist but no labels - show no labels message
                                totalTasks.isNotEmpty() && labelData.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🏷️",
                                                fontSize = 48.sp,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                            Text(
                                                text = "No labels found",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF616161),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Create tasks with labels to organize them better, or search above",
                                                fontSize = 16.sp,
                                                color = Color(0xFF9E9E9E),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }

                                // Sub-case 2c: Both tasks and labels exist - show ready to search message
                                totalTasks.isNotEmpty() && labelData.isNotEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "🔍",
                                                fontSize = 48.sp,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                            Text(
                                                text = "Ready to search!",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF616161),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Type above to search tasks or select a label below to filter",
                                                fontSize = 16.sp,
                                                color = Color(0xFF9E9E9E),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }

                                // Fallback case
                                else -> {
                                    LottieEmptyState(currentRoute = currentRoute)
                                }
                            }
                        }
                    }

                    // Case 3: Has search results - show the tasks
                    groupedTasks.isNotEmpty() -> {
                        groupedTasks.forEach { group ->
                            item(key = "header_${group.title}") {
                                TaskGroupHeader(group = group)
                            }

                            items(
                                items = group.tasks,
                                key = { task -> "${group.title}_${task.id}" }
                            ) { task ->
                                SwipeableTaskItem(
                                    task = task,
                                    viewModel = viewModel,
                                    currentRoute = currentRoute,
                                    undoToastManager = undoToastManager,
                                    coroutineScope = coroutineScope,
                                    keyboardController = keyboardController,
                                    focusManager = focusManager,
                                    modifier = Modifier.animateItemPlacement() // Simple animation!
                                )
                            }

                            item(key = "spacer_${group.title}") {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Handle other routes (today, inbox)
            (currentRoute == "today" || currentRoute == "inbox") && groupedTasks.isEmpty() -> {
                item {
                    LottieEmptyState(currentRoute = currentRoute)
                }
            }

            // Default case: show tasks for non-search routes
            else -> {
                groupedTasks.forEach { group ->
                    item(key = "header_${group.title}") {
                        TaskGroupHeader(group = group)
                    }

                    items(
                        items = group.tasks,
                        key = { task -> "${group.title}_${task.id}" }
                    ) { task ->
                        SwipeableTaskItem(
                            task = task,
                            viewModel = viewModel,
                            currentRoute = currentRoute,
                            undoToastManager = undoToastManager,
                            coroutineScope = coroutineScope,
                            keyboardController = keyboardController,
                            focusManager = focusManager,
                            modifier = Modifier.animateItemPlacement() // Simple animation!
                        )
                    }

                    item(key = "spacer_${group.title}") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Update SwipeableTaskItem to accept modifier
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableTaskItem(
    task: Task,
    viewModel: TaskViewModel,
    currentRoute: String,
    undoToastManager: UndoToastManager,
    coroutineScope: CoroutineScope,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    modifier: Modifier = Modifier // Add modifier parameter
) {
    // State to trigger reset
    var shouldResetDismissState by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToEnd -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) // Vibration
                    viewModel.setTaskToUpdate(task)
                    viewModel.setShowDatePicker(true)
                    viewModel.setId(task.id)
                    // Set flag to reset outside lambda
                    shouldResetDismissState = true
                    false // prevent dismissal
                }
                DismissValue.DismissedToStart -> {
                    coroutineScope.launch {
                        undoToastManager.showTaskDeletedToast(
                            taskName = task.title,
                            onDelete = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) // Vibration
                                viewModel.deleteTask(task.id)
                            },
                            onRestore = {
                                viewModel.restoreTask(task.id)
                            }
                        )
                    }
                    true // allow dismissal
                }
                else -> false
            }
        }
    )

    // Reset dismiss state when flag is set
    LaunchedEffect(shouldResetDismissState) {
        if (shouldResetDismissState) {
            dismissState.reset()
            shouldResetDismissState = false
        }
    }

    Box(modifier = modifier.padding(vertical = 4.dp)) { // Apply the modifier here
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
            // Increased threshold for less sensitivity - requires 60% swipe to trigger
            dismissThresholds = { FractionalThreshold(0.6f) },
            background = {
                // ... your existing background code ...
                val direction = dismissState.dismissDirection
                val progress = dismissState.progress.fraction

                // Only show color when actually swiping and past a certain threshold
                val shouldShowBackground = direction != null && progress > 0.2f

                val color by animateColorAsState(
                    targetValue = if (shouldShowBackground) {
                        when (direction) {
                            DismissDirection.StartToEnd -> colorResource(id = R.color.orange)
                            DismissDirection.EndToStart -> Color.Red
                            null -> Color.Transparent
                        }
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(150), // Faster animation for more responsive feel
                    label = "swipe_background_color"
                )

                val alignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                    null -> Alignment.Center
                }

                // Smooth icon animations without jarring transitions
                val iconScale by animateFloatAsState(
                    targetValue = if (shouldShowBackground) {
                        // Smooth continuous scaling curve
                        val baseScale = 0.4f + (progress * 0.8f) // Linear base from 0.4 to 1.2
                        val pulseMultiplier = if (progress > 0.7f) {
                            1f + (sin((progress - 0.7f) * 15f) * 0.05f) // Very subtle pulse
                        } else 1f
                        baseScale * pulseMultiplier
                    } else 0f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "icon_scale"
                )

                val iconAlpha by animateFloatAsState(
                    targetValue = if (shouldShowBackground) {
                        // Smooth fade curve
                        when {
                            progress < 0.2f -> 0f
                            progress < 0.5f -> (progress - 0.2f) / 0.3f // Smooth fade from 0.2 to 0.5
                            else -> 1f
                        }
                    } else 0f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "icon_alpha"
                )

                val iconRotation by animateFloatAsState(
                    targetValue = if (shouldShowBackground) {
                        when (direction) {
                            DismissDirection.StartToEnd -> progress * 12f // Smooth rotation
                            DismissDirection.EndToStart -> -progress * 15f
                            null -> 0f
                        }
                    } else 0f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "icon_rotation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    if (shouldShowBackground && iconScale > 0f) {
                        when (direction) {
                            DismissDirection.StartToEnd -> {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Smooth scaling background circle
                                    Box(
                                        modifier = Modifier
                                            .size((40f + (iconScale * 20f)).dp) // Smooth size progression
                                            .background(
                                                Color.White.copy(alpha = 0.15f * iconAlpha),
                                                CircleShape
                                            )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Set Date",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .scale(iconScale)
                                            .alpha(iconAlpha)
                                            .rotate(iconRotation)
                                    )
                                }
                            }
                            DismissDirection.EndToStart -> {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Single smooth scaling background - no secondary ring
                                    Box(
                                        modifier = Modifier
                                            .size((40f + (iconScale * 25f)).dp)
                                            .background(
                                                Color.White.copy(alpha = 0.2f * iconAlpha),
                                                CircleShape
                                            )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Task",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .scale(iconScale)
                                            .alpha(iconAlpha)
                                            .rotate(iconRotation)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            dismissContent = {
                TaskItem(task, viewModel, currentRoute, undoToastManager) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.setId(task.id)
                    viewModel.setTaskBeingEdited(true)
                    viewModel.setShowBottomSheet(true)
                }
            }
        )
    }
}

@Composable
private fun TaskGroupHeader(group: TaskGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(group.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${group.title} (${group.tasks.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = group.color
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}