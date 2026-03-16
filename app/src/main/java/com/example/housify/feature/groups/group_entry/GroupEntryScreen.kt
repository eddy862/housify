package com.example.housify.feature.groups.group_entry

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.QrCodeGenerator
import com.example.housify.R
import com.example.housify.convertDateToDayOfWeek
import com.example.housify.domain.model.LeaderboardEntry
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Space
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.User
import com.example.housify.domain.model.UserRole
import com.example.housify.joinGroupURI
import com.example.housify.ui.components.DualActionDialog
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.components.SimpleConfirmationDialogCard
import com.example.housify.ui.components.UserAvatar
import com.example.housify.ui.theme.HousifyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GroupEntryScreen(
    onSelectLeaderboardHistory: () -> Unit,
    onSelectCreateTask: () -> Unit,
    onSelectTaskDetails: (String) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: GroupEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.getGroupEntryLoading -> {
            LoadingFullScreen()
        }

        uiState.getGroupEntryError != null -> {
            ErrorFullScreen(uiState.getGroupEntryError, viewModel::refreshData)
        }

        else -> {
            var isAdminEditGroupDialogOpen by remember { mutableStateOf(false) }
            var isAdminQRDialogOpen by remember { mutableStateOf(false) }
            var selectedUser by remember { mutableStateOf<User?>(null) }
            var isMemberLeaveGroupDialogOpen by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.successMessage.collect { message ->
                    onShowSnackbar(message)
                }
            }

            LaunchedEffect(Unit) {
                viewModel.closeDialogEvent.collect {
                    isAdminQRDialogOpen = false
                    isAdminEditGroupDialogOpen = false
                    isMemberLeaveGroupDialogOpen = false
                    selectedUser = null
                }
            }

            LaunchedEffect(Unit) {
                viewModel.onBackEvent.collect {
                    onBack()
                }
            }

            val useRole = if (uiState.groupEntry!!.isUserAdmin) UserRole.ADMIN else UserRole.MEMBER

            val inviteUri =
                joinGroupURI.replace("{invitationCode}", uiState.groupEntry!!.invitationCode)
            var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

            LaunchedEffect(inviteUri) {
                launch(Dispatchers.IO) {
                    qrCodeBitmap = QrCodeGenerator.generate(inviteUri)
                }
            }

            when {
                isAdminEditGroupDialogOpen -> {
                    DualActionDialog(
                        onDismissRequest = { isAdminEditGroupDialogOpen = false },
                        inputPrefixId = R.drawable.group_name,
                        inputPrefixContentDesc = "group name",
                        inputPlaceholder = "Group name",
                        onConfirmTop = { groupName -> viewModel.editGroupName(groupName) },
                        buttonTextTop = "Save",
                        onConfirmBottom = { viewModel.deleteGroup() },
                        buttonTextBottom = "Leave & Delete Group",
                        loadingTop = uiState.editGroupNameLoading,
                        errorTop = uiState.editGroupNameError,
                        loadingBottom = uiState.deleteGroupLoading,
                        errorBottom = uiState.deleteGroupError,
                        initialText = uiState.groupEntry!!.name
                    )
                }

                isAdminQRDialogOpen -> {
                    Dialog(onDismissRequest = { isAdminQRDialogOpen = false }) {
                        QRDialogCard(
                            onDismissRequest = { isAdminQRDialogOpen = false },
                            invitationCode = uiState.groupEntry!!.invitationCode,
                            qrCodeBitmap = qrCodeBitmap,
                            onShowSnackbar = onShowSnackbar
                        )
                    }
                }

                selectedUser != null -> {
                    val role =
                        if (selectedUser!!.id == uiState.groupEntry!!.admin.id) UserRole.ADMIN
                        else UserRole.MEMBER

                    Dialog(onDismissRequest = { selectedUser = null }) {
                        ClickUserAvatarDialogCard(
                            selectedUserRole = role,
                            onDismissRequest = { selectedUser = null },
                            userName = selectedUser!!.name,
                            onRemoveMember = { viewModel.removeMember(selectedUser!!) },
                            buttonText = "Remove Member",
                            showButton = uiState.groupEntry!!.isUserAdmin,
                            loading = uiState.removeMemberLoading,
                            error = uiState.removeMemberError
                        )
                    }
                }

                isMemberLeaveGroupDialogOpen -> {
                    Dialog(onDismissRequest = { isMemberLeaveGroupDialogOpen = false }) {
                        SimpleConfirmationDialogCard(
                            onDismissRequest = { isMemberLeaveGroupDialogOpen = false },
                            title = "Leave Group",
                            content = "Are you sure you want to leave '${uiState.groupEntry!!.name}'?",
                            onConfirm = { viewModel.memberLeaveGroup() },
                            confirmText = "Leave",
                            loading = uiState.memberLeaveGroupLoading,
                            error = uiState.memberLeaveGroupError
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
                        GroupNameActionCard(
                            groupName = uiState.groupEntry!!.name,
                            userRole = useRole,
                            onAdminEditGroup = { isAdminEditGroupDialogOpen = true },
                            onMemberLeaveGroup = { isMemberLeaveGroupDialogOpen = true },
                            onShowQR = { isAdminQRDialogOpen = true }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    item {
                        RolesCard(
                            admin = uiState.groupEntry!!.admin,
                            members = uiState.groupEntry!!.members,
                            onClickUserAvatar = { member ->
                                selectedUser = member
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    item {
                        LeaderboardCard(
                            uiState.groupEntry!!.latestLeaderboardEntries,
                            onSelectLeaderboardHistory
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    item {
                        TasksHeader(uiState.groupEntry!!.tasks.size, useRole, onSelectCreateTask)
                    }

                    if (uiState.groupEntry!!.tasks.isEmpty()) {
                        item {
                            Text(
                                "No tasks",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp)
                            )
                        }
                    } else {
                        viewModel.getTasksGroupedBySpace()?.forEach { (space, tasks) ->
                            item {
                                TaskEntryHeader(space)
                            }

                            items(tasks, key = { it.id }) { task ->
                                TaskEntryCard(onSelectTaskDetails, task)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClickUserAvatarDialogCard(
    selectedUserRole: UserRole,
    onDismissRequest: () -> Unit,
    userName: String,
    onRemoveMember: () -> Unit,
    buttonText: String,
    showButton: Boolean = true,
    loading: Boolean,
    error: String?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel),
                        contentDescription = "close",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            UserAvatar(username = userName, size = 100.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    userName,
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    if (selectedUserRole == UserRole.ADMIN) " (Admin)"
                    else " (Member)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showButton) {
                Spacer(modifier = Modifier.height(15.dp))
                Button(
                    onClick = onRemoveMember,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text((buttonText).uppercase())
                    }
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AdminEditMemberDialogCardPreview() {
    HousifyTheme {
        ClickUserAvatarDialogCard(
            onDismissRequest = {},
            userName = "John Doe",
            onRemoveMember = {},
            buttonText = "Remove Member",
            selectedUserRole = UserRole.ADMIN,
            loading = false,
            error = null
        )
    }
}

@Composable
fun QRDialogCard(
    onDismissRequest: () -> Unit,
    invitationCode: String,
    qrCodeBitmap: ImageBitmap?,
    onShowSnackbar: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel),
                        contentDescription = "close",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(modifier = Modifier.size(300.dp)) {
                if (qrCodeBitmap != null) {
                    Image(
                        bitmap = qrCodeBitmap,
                        contentDescription = "QR code",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator()
                }
            }


            Text(
                "Invitation Code",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(invitationCode))
                        onShowSnackbar("Invitation code copied to clipboard")
                    }
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    invitationCode,
                    style = MaterialTheme.typography.titleLarge
                )

                Icon(
                    painter = painterResource(R.drawable.content_copy),
                    contentDescription = "Copy invitation code",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
fun QRDialogCardPreview() {
    HousifyTheme {
        QRDialogCard(
            onDismissRequest = {},
            invitationCode = "111222",
            qrCodeBitmap = QrCodeGenerator.generate("111"),
            onShowSnackbar = {}
        )
    }
}

@Composable
fun GroupNameActionCard(
    groupName: String,
    userRole: UserRole,
    onAdminEditGroup: () -> Unit,
    onMemberLeaveGroup: () -> Unit,
    onShowQR: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(groupName, style = MaterialTheme.typography.titleMedium)

            Row {
                IconButton(
                    onClick = if (userRole == UserRole.ADMIN) onAdminEditGroup
                    else onMemberLeaveGroup
                ) {
                    Icon(
                        painter = painterResource(
                            if (userRole == UserRole.ADMIN) R.drawable.edit_group
                            else R.drawable.leave_group
                        ),
                        contentDescription = "Edit group",
                        modifier = Modifier.size(30.dp)
                    )
                }

                if (userRole == UserRole.ADMIN) {
                    IconButton(onClick = onShowQR) {
                        Icon(
                            painter = painterResource(R.drawable.qr_group),
                            contentDescription = "Group QR code",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskEntryHeader(
    space: Space
) {
    Spacer(modifier = Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(space.icon),
            contentDescription = space.name,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            space.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun TaskEntryCard(
    onSelectTaskDetails: (String) -> Unit,
    task: Task
) {
    ElevatedCard(
        onClick = { onSelectTaskDetails(task.id) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (task.assignedToCurrentUser) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.group_task_remind),
                        contentDescription = "Assigned",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Start from ${task.startDate}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.access_time),
                    contentDescription = task.repeat.toString(),
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "${
                        task.repeat.toString().lowercase()
                            .replaceFirstChar { it.uppercase() }
                    }${
                        if (task.repeat == Repeat.WEEKLY) {
                            " on ${convertDateToDayOfWeek(task.startDate, true)}"
                        } else {
                            ""
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OverlappingAvatarRow(task.assignees)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${task.assignees.size} assignees",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (task.rotationEnabled) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "(In rotation)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun TasksHeader(
    taskSize: Int,
    userRole: UserRole,
    onSelectCreateTask: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Tasks (${taskSize})",
            style = MaterialTheme.typography.titleLarge
        )
        if (userRole == UserRole.ADMIN) {
            IconButton(onClick = onSelectCreateTask) {
                Icon(
                    painter = painterResource(R.drawable.create_group),
                    contentDescription = "Create task",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }

    if (userRole == UserRole.MEMBER) {
        Spacer(modifier = Modifier.height(10.dp))
    }

    HorizontalDivider()
}

@Composable
private fun OverlappingAvatarRow(
    assignees: List<User>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-10).dp)
    ) {
        items(assignees) { assignee ->
            UserAvatar(
                username = assignee.name,
                border = true,
                size = 30.dp
            )
        }
    }
}

@Composable
fun LeaderboardCard(
    leaderboardEntries: List<LeaderboardEntry>,
    onSelectLeaderboardHistory: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(
                start = 10.dp,
                end = 10.dp,
                top = 20.dp,
                bottom = 10.dp
            )
        ) {
            Text("Leaderboard", style = MaterialTheme.typography.titleLarge)

            if (leaderboardEntries.isEmpty()) {
                Text(
                    "Not available",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
            } else {


                Text(
                    "Average Rating",
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(5.dp))

                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    leaderboardEntries.forEachIndexed { index, entry ->
                        LeaderboardEntry(
                            index = index,
                            entry = entry
                        )
                    }
                }

                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onSelectLeaderboardHistory
                ) {
                    Text(
                        "View History >",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardEntry(
    index: Int,
    entry: LeaderboardEntry
) {
    val trophyColors: List<Color> = listOf(
        Color(0xFFFFCC00),
        Color(0xFF979797),
        Color(0xFFDD4D00)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text((index + 1).toString())
                Spacer(modifier = Modifier.width(15.dp))
                UserAvatar(entry.user.name)
                Spacer(modifier = Modifier.width(10.dp))
                Text(entry.user.name)
            }

            if (index < 3) {
                Icon(
                    painter = painterResource(R.drawable.trophy),
                    contentDescription = "Trophy",
                    modifier = Modifier.size(25.dp),
                    tint = trophyColors[index]
                )
            }
        }

        Text(
            entry.rating.toString(),
            modifier = Modifier.weight(0.35f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RolesCard(
    admin: User,
    members: List<User>,
    onClickUserAvatar: (User) -> Unit,
) {
    fun onClickUserAvatarChecked(user: User) =
        if (!user.isCurrentUser) onClickUserAvatar(user) else {
        }

    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Roles", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Admin", style = MaterialTheme.typography.labelLarge)
            UserAvatar(
                username = admin.name,
                size = 50.dp,
                modifier = Modifier
                    .padding(10.dp),
                border = admin.isCurrentUser,
                borderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onClickUserAvatarChecked(admin) }
            )

            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Text("Members (${members.size})", style = MaterialTheme.typography.labelLarge)
            LazyRow(
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(members) { it ->
                    UserAvatar(
                        username = it.name,
                        size = 50.dp,
                        border = it.isCurrentUser,
                        borderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { onClickUserAvatarChecked(it) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupEntryPreview() {
    HousifyTheme() {
        GroupEntryScreen(
            onSelectLeaderboardHistory = {},
            onSelectCreateTask = {},
            onSelectTaskDetails = {},
            onShowSnackbar = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
fun OverlappingAvatarsPreview() {
    OverlappingAvatarRow(
        assignees = listOf(
            User("123", "user1"),
            User("456", "user2"),
            User("789", "user3"),
            User("101", "user4"),
        )
    )
}

