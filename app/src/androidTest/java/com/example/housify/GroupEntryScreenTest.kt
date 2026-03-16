package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.domain.model.*
import com.example.housify.domain.model.GroupEntry
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.feature.groups.group_entry.ClickUserAvatarDialogCard
import com.example.housify.feature.groups.group_entry.GroupEntryScreen
import com.example.housify.feature.groups.group_entry.GroupEntryViewModel
import com.example.housify.feature.groups.group_entry.LeaderboardCard
import com.example.housify.feature.groups.group_entry.QRDialogCard
import com.example.housify.feature.groups.group_entry.TaskEntryCard
import com.example.housify.feature.groups.group_entry.TasksHeader
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupEntryScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: GroupRepository
    private lateinit var viewModel: GroupEntryViewModel

    private val fakeGroupId = "group123"

    private val fakeAdmin = User(
        id = "admin1",
        name = "Admin User",
        isCurrentUser = true
    )

    private val fakeMember = User(
        id = "member1",
        name = "Member One",
        isCurrentUser = false
    )

    private val fakeLeaderboardEntry = LeaderboardEntry(
        user = fakeAdmin,
        rating = 4.5f
    )

    private val fakeTask = Task(
        id = "task1",
        groupId = fakeGroupId,
        title = "Clean Room",
        startDate = "2025/01/01",
        repeat = Repeat.WEEKLY,
        rotationEnabled = true,
        space = PredefinedSpace.Bathroom,
        assignees = listOf(fakeAdmin, fakeMember),
        assignedToCurrentUser = true
    )

    private val fakeGroupEntry = GroupEntry(
        id = fakeGroupId,
        name = "Testing Group",
        admin = fakeAdmin,
        members = listOf(fakeMember),
        latestLeaderboardEntries = listOf(fakeLeaderboardEntry),
        tasks = listOf(fakeTask),
        isUserAdmin = true,
        invitationCode = "123456"
    )

    @Before
    fun setup() {
        repository = mockk()

        coEvery { repository.getGroupEntry(fakeGroupId) } returns flowOf(
            Resource.Success(fakeGroupEntry)
        )

        val savedStateHandle = SavedStateHandle(
            mapOf("groupId" to fakeGroupId)
        )

        viewModel = GroupEntryViewModel(
            groupRepository = repository,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun groupEntryScreen_shows_group_name_and_handles_clicks() {
        // Arrange
        var historyClicked = false
        var createTaskClicked = false
        var clickedTaskId: String? = null

        // Act
        composeRule.setContent {
            GroupEntryScreen(
                onSelectLeaderboardHistory = { historyClicked = true },
                onSelectCreateTask = { createTaskClicked = true },
                onSelectTaskDetails = { id -> clickedTaskId = id },
                onShowSnackbar = {},
                onBack = {},
                viewModel = viewModel
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            val state = viewModel.uiState.value
            !state.getGroupEntryLoading && state.groupEntry != null
        }

        // Assert
        composeRule.onNodeWithText("Testing Group")
            .assertExists()
        composeRule.onNodeWithText("Tasks (1)")
            .assertExists()
        composeRule.onNodeWithText("Clean Room")
            .assertExists()
        composeRule.onNodeWithText("Leaderboard")
            .assertExists()
        composeRule.onNodeWithText("View History >")
            .performClick()
        assertThat(historyClicked).isTrue()
        composeRule.onNodeWithContentDescription("Create task")
            .performClick()
        assertThat(createTaskClicked).isTrue()
        composeRule.onNodeWithText("Clean Room")
            .performClick()
        assertThat(clickedTaskId).isEqualTo("task1")
    }

    @Test
    fun leaderboardCard_clickViewHistory_calls_onSelectLeaderboardHistory() {
        // Arrange
        var called = false

        val entries = listOf(
            LeaderboardEntry(
                user = User(id = "1", name = "Alice", isCurrentUser = false),
                rating = 4.5f
            )
        )

        // Act
        composeRule.setContent {
            MaterialTheme {
                LeaderboardCard(
                    leaderboardEntries = entries,
                    onSelectLeaderboardHistory = { called = true }
                )
            }
        }

        composeRule.onNodeWithText("View History >")
            .performClick()

        // Assert
        assertThat(called).isTrue()
    }

    @Test
    fun tasksHeader_adminClickCreate_calls_onSelectCreateTask() {
        // Arrange
        var called = false

        // Act
        composeRule.setContent {
            MaterialTheme {
                TasksHeader(
                    taskSize = 3,
                    userRole = UserRole.ADMIN,
                    onSelectCreateTask = { called = true }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Create task")
            .performClick()

        // Assert
        assertThat(called).isTrue()
    }

    @Test
    fun tasksHeader_member_doesNotShowCreateButton() {
        // Act
        composeRule.setContent {
            MaterialTheme {
                TasksHeader(
                    taskSize = 3,
                    userRole = UserRole.MEMBER,
                    onSelectCreateTask = {}
                )
            }
        }

        // Assert
        composeRule.onNodeWithContentDescription("Create task")
            .assertDoesNotExist()
    }

    @Test
    fun taskEntryCard_click_calls_onSelectTaskDetails_withCorrectId() {
        // Arrange
        var clickedTaskId: String? = null

        val assignee = User(id = "u1", name = "Alice", isCurrentUser = false)
        val task = Task(
            id = "task123",
            groupId = "group1",
            title = "Clean Room",
            startDate = "2025-01-01",
            repeat = Repeat.NONE,
            rotationEnabled = false,
            space = PredefinedSpace.Bathroom,
            assignees = listOf(assignee),
            assignedToCurrentUser = true
        )

        // Act
        composeRule.setContent {
            MaterialTheme {
                TaskEntryCard(
                    onSelectTaskDetails = { clickedTaskId = it },
                    task = task
                )
            }
        }

        composeRule.onNodeWithText("Clean Room")
            .performClick()

        // Assert
        assertThat(clickedTaskId).isEqualTo("task123")
    }

    @Test
    fun qrDialog_clickInvitationCode_calls_onShowSnackbar() {
        // Arrange
        var snackbarMessage: String? = null

        // Act
        composeRule.setContent {
            MaterialTheme {
                QRDialogCard(
                    onDismissRequest = {},
                    invitationCode = "ABC1234",
                    qrCodeBitmap = null as ImageBitmap?,
                    onShowSnackbar = { msg -> snackbarMessage = msg }
                )
            }
        }

        composeRule.onNodeWithText("ABC1234")
            .performClick()

        // Assert
        assertThat(snackbarMessage).isEqualTo("Invitation code copied to clipboard")
    }

    @Test
    fun clickUserAvatarDialog_removeButton_calls_onRemoveMember() {
        // Arrange
        var removed = false

        // Act
        composeRule.setContent {
            MaterialTheme {
                ClickUserAvatarDialogCard(
                    selectedUserRole = UserRole.ADMIN,
                    onDismissRequest = {},
                    userName = "John Doe",
                    onRemoveMember = { removed = true },
                    buttonText = "Remove Member",
                    showButton = true,
                    loading = false,
                    error = null
                )
            }
        }

        composeRule.onNodeWithText("REMOVE MEMBER")
            .performClick()

        // Assert
        assertThat(removed).isTrue()
    }

}
