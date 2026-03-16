package com.example.housify

import androidx.lifecycle.SavedStateHandle
import com.example.housify.domain.model.GroupEntry
import com.example.housify.domain.model.LeaderboardEntry
import com.example.housify.domain.model.PredefinedSpace.Bathroom
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.User
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.feature.groups.group_entry.GroupEntryViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: GroupRepository
    private lateinit var viewModel: GroupEntryViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeGroupId = "group123"

    private val fakeAdmin = User(
        id = "admin123",
        name = "Admin User",
        isCurrentUser = true
    )

    private val fakeMember = User(
        id = "member456",
        name = "Normal Member",
        isCurrentUser = false
    )

    private val fakeLeaderboardEntry = LeaderboardEntry(
        user = fakeAdmin,
        rating = 4.5f
    )

    private val fakeTask = Task(
        id = "task123",
        groupId = "group123",
        title = "Clean Room",
        startDate = "2025-01-01",
        repeat = Repeat.NONE,
        rotationEnabled = false,
        space = Bathroom,
        assignees = listOf(fakeAdmin),
        assignedToCurrentUser = true
    )

    private val fakeGroupEntry = GroupEntry(
        id = "group123",
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
        repository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("groupId" to fakeGroupId))
    }

    @Test
    fun `fetchLatestData emits Loading then Success`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { repository.getGroupEntry(fakeGroupId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(fakeGroupEntry)
        )

        // Act
        viewModel = GroupEntryViewModel(repository, savedStateHandle)

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.groupEntry).isEqualTo(fakeGroupEntry)
    }

    @Test
    fun `editGroupName emits Loading then Success`() = runTest {
        // Arrange
        coEvery { repository.editGroupName(fakeGroupId, "NewName") } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )
        coEvery { repository.getGroupEntry(fakeGroupId) } returns flowOf(
            Resource.Success(fakeGroupEntry)
        )

        // Act
        viewModel = GroupEntryViewModel(repository, savedStateHandle)
        viewModel.editGroupName("NewName")
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.editGroupNameLoading).isFalse()
        assertThat(state.editGroupNameError).isNull()
    }

    @Test
    fun `deleteGroup emits Loading then Success`() = runTest {
        // Arrange
        coEvery { repository.getGroupEntry(fakeGroupId) } returns flowOf(
            Resource.Success(fakeGroupEntry)
        )

        viewModel = GroupEntryViewModel(repository, savedStateHandle)

        coEvery { repository.deleteGroup(fakeGroupId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )

        // Act
        viewModel.deleteGroup()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.deleteGroupLoading).isFalse()
        assertThat(state.deleteGroupError).isNull()
    }

    @Test
    fun `removeMember emits Loading then Success`() = runTest {
        // Arrange
        coEvery { repository.removeMember(fakeGroupId, fakeMember.id) } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )

        coEvery { repository.getGroupEntry(fakeGroupId) } returns flowOf(
            Resource.Success(fakeGroupEntry)
        )

        // Act
        viewModel = GroupEntryViewModel(repository, savedStateHandle)
        viewModel.removeMember(fakeMember)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.removeMemberLoading).isFalse()
        assertThat(state.removeMemberError).isNull()
    }

    @Test
    fun `memberLeaveGroup emits Loading then Success`() = runTest {
        // Arrange
        coEvery { repository.getGroupEntry(fakeGroupId) } returns flowOf(
            Resource.Success(fakeGroupEntry)
        )

        viewModel = GroupEntryViewModel(repository, savedStateHandle)

        coEvery { repository.leaveGroup(fakeGroupId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )

        // Act
        viewModel.memberLeaveGroup()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.memberLeaveGroupLoading).isFalse()
        assertThat(state.memberLeaveGroupError).isNull()
    }
}
