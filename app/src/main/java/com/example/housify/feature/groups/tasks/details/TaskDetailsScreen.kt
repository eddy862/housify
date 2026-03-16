package com.example.housify.feature.groups.tasks.details

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.R
import com.example.housify.convertDateToDayOfWeek
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.TaskSchedule
import com.example.housify.domain.model.User
import com.example.housify.domain.model.UserRole
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.components.SimpleConfirmationDialogCard
import com.example.housify.ui.components.UserAvatar
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun TaskDetailsScreen(
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.getTaskLoading -> {
            LoadingFullScreen()
        }

        uiState.getTaskError != null -> {
            ErrorFullScreen(uiState.getTaskError, viewModel::refreshData)
        }

        else -> {
            if (uiState.taskDetails!!.recentSchedule.isEmpty()) {
                ErrorFullScreen("No recent schedule found", viewModel::refreshData)
            } else {
                val userRole =
                    if (uiState.taskDetails!!.isUserAdmin) UserRole.ADMIN else UserRole.MEMBER

                var isDeleteTaskDialogOpen by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    viewModel.successMessage.collect { message ->
                        onShowSnackbar(message)
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.onBackEvent.collect {
                        onBack()
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.closeDialogEvent.collect {
                        isDeleteTaskDialogOpen = false
                    }
                }

                when {
                    isDeleteTaskDialogOpen -> {
                        Dialog(onDismissRequest = { isDeleteTaskDialogOpen = false }) {
                            SimpleConfirmationDialogCard(
                                onDismissRequest = { isDeleteTaskDialogOpen = false },
                                title = "Delete Task",
                                content = "Are you sure you want to delete this task?",
                                onConfirm = { viewModel.deleteTask() },
                                confirmText = "Delete",
                                loading = uiState.deleteTaskLoading,
                                error = uiState.deleteTaskError
                            )
                        }
                    }
                }

                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = { viewModel.refreshData() },
                ) {
                    LazyColumn {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp, horizontal = 15.dp)
                                ) {
                                    TaskDetailHeader(
                                        task = uiState.taskDetails!!.task,
                                        currentUserRole = userRole,
                                        onDeleteTask = { isDeleteTaskDialogOpen = true },
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                    Text(
                                        "Assignees (${uiState.taskDetails!!.task.assignees.size})",
                                        style = MaterialTheme.typography.titleMedium
                                    )


                                    when (uiState.taskDetails!!.task.rotationEnabled) {
                                        true -> {
                                            TaskAssigneesContentWithRotation(
                                                assignees = uiState.taskDetails!!.task.assignees,
                                                nextCleaningSchedule = uiState.taskDetails!!.recentSchedule
                                            )
                                        }

                                        false -> {
                                            Spacer(modifier = Modifier.height(15.dp))
                                            TaskAssigneesContentWithoutRotation(
                                                assignees = uiState.taskDetails!!.task.assignees,
                                                nextCleaningDate = uiState.taskDetails!!.recentSchedule.first().date
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskAssigneesContentWithRotation(
    assignees: List<User>,
    nextCleaningSchedule: List<TaskSchedule>,
) {
    Column() {
        Text(
            "Next cleaning date",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            assignees.forEach { assignee ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        UserAvatar(assignee.name)
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            assignee.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "${nextCleaningSchedule.firstOrNull { it.user.id == assignee.id }?.date}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(90.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
fun TaskAssigneesContentWithoutRotation(
    assignees: List<User>,
    nextCleaningDate: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            items(assignees) { it ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    UserAvatar(
                        username = it.name,
                        size = 50.dp
                    )
                    Text(
                        it.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        }

        Text(
            "Next cleaning date: $nextCleaningDate",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun TaskDetailHeader(
    task: Task,
    currentUserRole: UserRole,
    onDeleteTask: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (task.assignedToCurrentUser) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.group_task_remind),
                        contentDescription = "Assigned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (currentUserRole == UserRole.ADMIN) {
                IconButton(onClick = onDeleteTask) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete task",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Text(
            "Start from ${task.startDate}",
            style = MaterialTheme.typography.bodyMedium
        )

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(8.dp),   // Spacing between rows when items wrap
            horizontalArrangement = Arrangement.spacedBy(15.dp) // Spacing between items on the same row
        ) {
            TaskPropertyWithIcon(
                icon = task.space.icon,
                contentDesc = task.space.name,
                text = task.space.name
            )

            TaskPropertyWithIcon(
                icon = R.drawable.access_time,
                contentDesc = "Repeat setting",
                text = "${task.repeat.displayText}${
                    if (task.repeat == Repeat.WEEKLY) {
                        " on ${convertDateToDayOfWeek(task.startDate, true)}"
                    } else {
                        ""
                    }
                }"
            )

            if (task.rotationEnabled) {
                TaskPropertyWithIcon(
                    icon = R.drawable.rotation,
                    contentDesc = "Rotation Enable",
                    text = "In rotation"
                )
            }
        }
    }
}

@Composable
fun TaskPropertyWithIcon(
    @DrawableRes icon: Int,
    contentDesc: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDesc,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun TaskDetailsPreview() {
    HousifyTheme {
        TaskDetailsScreen(
            onBack = {},
            onShowSnackbar = {}
        )
    }
}

