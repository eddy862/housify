package com.example.housify.feature.groups.groups_list

import androidx.annotation.DrawableRes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.R
import com.example.housify.domain.model.Group
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.components.SimpleDialog
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun GroupScreen(
    onGroupSelect: (String) -> Unit,
    onShowSnackbar: (String) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var isCreateGroupDialogOpen by remember { mutableStateOf(false) }
    var isJoinGroupDialogOpen by remember { mutableStateOf(false) }
    val dialogButtonEnabled = uiState.getGroupsError == null && !uiState.getGroupsLoading

    LaunchedEffect(Unit) {
        viewModel.successMessage.collect { message ->
            onShowSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.closeDialogEvent.collect {
            isCreateGroupDialogOpen = false
            isJoinGroupDialogOpen = false
        }
    }

    when {
        isCreateGroupDialogOpen -> {
            SimpleDialog(
                onDismissRequest = { isCreateGroupDialogOpen = false },
                inputPrefixId = R.drawable.group_name,
                inputPrefixContentDesc = "group name",
                inputPlaceholder = "Group name",
                onConfirm = { groupName ->
                    viewModel.createGroup(groupName)
                },
                buttonText = "Create",
                loading = uiState.createGroupLoading,
                error = uiState.createGroupError
            )
        }

        isJoinGroupDialogOpen -> {
            SimpleDialog(
                onDismissRequest = { isJoinGroupDialogOpen = false },
                inputPrefixId = R.drawable.invitation_code,
                inputPrefixContentDesc = "invitation code",
                inputPlaceholder = "Invitation code",
                onConfirm = { invitationCode ->
                    viewModel.joinGroup(invitationCode)
                },
                buttonText = "Join",
                loading = uiState.joinGroupLoading,
                error = uiState.joinGroupError
            )
        }
    }

    Column() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Groups (${uiState.groups.size})",
                style = MaterialTheme.typography.titleLarge
            )

            Row {
                TopIconButton(
                    onClick = { isCreateGroupDialogOpen = true },
                    resId = R.drawable.create_group,
                    contentDesc = "create group",
                    enabled = dialogButtonEnabled
                )
                TopIconButton(
                    onClick = { isJoinGroupDialogOpen = true },
                    resId = R.drawable.join_group,
                    contentDesc = "join group",
                    enabled = dialogButtonEnabled
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when {
            uiState.getGroupsLoading && uiState.groups.isEmpty() -> {
                LoadingFullScreen()
            }

            uiState.getGroupsError != null && uiState.groups.isEmpty() -> {
                ErrorFullScreen(uiState.getGroupsError, viewModel::refreshData)
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = false,
                    onRefresh = { viewModel.refreshData() },
                ) {

                    LazyColumn() {
                        if (!uiState.groups.isEmpty()) {
                            items(uiState.groups) { it ->
                                GroupEntryCard(
                                    onClick = onGroupSelect,
                                    group = it
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else {
                            item {
                                Text(
                                    "No group. Please create or join a group.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupEntryCard(
    onClick: (String) -> Unit,
    group: Group
) {
    val role = if (group.isUserAdmin) "admin" else "member"

    ElevatedCard(
        onClick = { onClick(group.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    role.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.no_group_members),
                    contentDescription = "Number of members",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "${group.members.size} members",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.group_task_remind),
                    contentDescription = "Number of assigned tasks",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "You've been assigned ${group.numberOfAssignedTasks} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TopIconButton(
    onClick: () -> Unit,
    @DrawableRes resId: Int,
    contentDesc: String,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(resId),
            contentDescription = contentDesc,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupsPreview() {
    HousifyTheme(dynamicColor = false) {
        GroupScreen(
            onGroupSelect = {},
            onShowSnackbar = {}
        )
    }
}



