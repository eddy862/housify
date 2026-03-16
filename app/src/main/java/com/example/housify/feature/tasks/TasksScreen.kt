package com.example.housify.feature.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.R
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.ui.components.SimpleAlertDialogCard
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel(),
    onTaskSelect: (TodayTask) -> Unit,
    onRatingSelect: (UncompletedRating) -> Unit
) {
    val scrollState = rememberScrollState()
    val selectedDateString by viewModel.selectedDate.collectAsState()

    var weekStartDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDate = remember(selectedDateString) {
        runCatching { LocalDate.parse(selectedDateString) }.getOrDefault(LocalDate.now())
    }

    val todayTasks by viewModel.todayTasks.collectAsState()
    val futureTasks by viewModel.futureTasks.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val taskToBeCompleted =
        if (selectedDate == LocalDate.now()) todayTasks else futureTasks
    val isTasksLoading by viewModel.isTasksLoading.collectAsState()
    val isRatingsLoading by viewModel.isRatingsLoading.collectAsState()

    var showFutureTaskDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showFutureTaskDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showFutureTaskDialog = false }
                ) {
                    SimpleAlertDialogCard(
                        onDismissRequest = { showFutureTaskDialog = false },
                        title = "Task not available yet",
                        content = "You can only complete this task on its scheduled date.",
                        onConfirm = { showFutureTaskDialog = false },
                        confirmText = "OK",
                        loading = false,
                        error = null
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        showDatePicker = true
                    },
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )

                        Text(
                            text = "More Date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (showDatePicker) {
                val initialMillis = remember(selectedDate) {
                    selectedDate
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli()
                }

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialMillis
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    val newDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()

                                    weekStartDate = newDate
                                    viewModel.setSelectedDate(newDate.toString())
                                }
                                showDatePicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            DateSelector(
                weekStartDate = weekStartDate,
                selectedDate = selectedDate
            ) { date ->
                viewModel.setSelectedDate(date.toString())
            }

            CustomTab(
                selectedDate
                    .format(
                        DateTimeFormatter.ofPattern("dd MMM yyyy, E", Locale.ENGLISH)
                    ), taskToBeCompleted.size.toString(), ratings.size.toString()
            )

            BigText("UNCOMPLETED TASKS")

            LaunchedEffect(selectedDate) {
                if (selectedDate == LocalDate.now()) {
                    viewModel.getTodayTasks()
                } else {
                    viewModel.getFutureTasks(selectedDate.toString())
                }
            }

            if (isTasksLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                if (selectedDate == LocalDate.now()) {
                    val tasksByGroup = todayTasks.groupBy { it.groupName }

                    if (tasksByGroup.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No tasks", color = MaterialTheme.colorScheme.onSurface)
                        }
                    } else {
                        tasksByGroup.forEach { (groupName, tasksInGroup) ->
                            // Group header
                            SmallText(groupName)

                            // Tasks under that group
                            tasksInGroup.forEach { task ->
                                TaskTab(
                                    title = task.title,
                                    location = task.place,
                                    clockOrPersonIcon = R.drawable.access_time,
                                    timeOrPerson = task.scheduleType,
                                    onClick = { onTaskSelect(task) },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                } else {
                    val tasksByGroup = futureTasks.groupBy { it.groupName }

                    if (tasksByGroup.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No tasks")
                        }
                    } else {
                        tasksByGroup.forEach { (groupName, tasksInGroup) ->
                            SmallText(groupName)

                            tasksInGroup.forEach { task ->
                                TaskTab(
                                    title = task.title,
                                    location = task.place,
                                    clockOrPersonIcon = R.drawable.access_time,
                                    timeOrPerson = task.scheduleType,
                                    onClick = { showFutureTaskDialog = true }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onBackground,
                thickness = 1.dp,
            )

            BigText("UNCOMPLETED RATINGS")

            LaunchedEffect(Unit) {
                viewModel.getUncompletedTask()
            }


            if (isRatingsLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                if (ratings.isNotEmpty()) {

                    // Group by groupName
                    val ratingsByGroup = ratings.groupBy { it.groupName }

                    ratingsByGroup.forEach { (groupName, groupRatings) ->

                        // Always show group header
                        SmallText(groupName)

                        groupRatings.forEach { rating ->
                            TaskTab(
                                title = rating.title,
                                location = rating.place,
                                clockOrPersonIcon = R.drawable.no_group_members,
                                timeOrPerson = rating.revieweeName,
                                onClick = { onRatingSelect(rating) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No ratings", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun BigText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
        )
    )
}

@Composable
fun SmallText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun TaskTab(
    title: String,
    location: String,
    clockOrPersonIcon: Int,
    timeOrPerson: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .clickable() {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.Black
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = location,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(clockOrPersonIcon),
                    contentDescription = "Clock or Person Icon",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = timeOrPerson,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getWeekDays(startDate: LocalDate = LocalDate.now()): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    repeat(7) { day ->
        dates.add(startDate.plusDays(day.toLong()))
    }
    return dates
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(
    weekStartDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val weekDays = remember(weekStartDate) { getWeekDays(weekStartDate) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(weekDays) { date ->
            DateChip(
                date = date,
                isSelected = (date == selectedDate),
                onDateSelected = { onDateSelected(date) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onPrimary

    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.inverseOnSurface

    val border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null

    Surface(
        modifier = Modifier.clickable { onDateSelected(date) },
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfWeek.name.take(3),  // e.g. MON, TUE
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CustomTab(time: String, tasks: String, ratings: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 30.dp)
    ) {
        Column() {
            Text(
                text = time,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tasks,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    )
                    Text(
                        text = "Tasks to be completed",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = ratings,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    )
                    Text(
                        text = "Ratings to be completed",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun TaskScreenPreview() {
    TasksScreen(
        onTaskSelect = {},
        onRatingSelect = {}
    )
}