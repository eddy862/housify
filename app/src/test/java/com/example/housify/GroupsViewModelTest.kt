package com.example.housify

import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.feature.groups.groups_list.GroupsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: GroupRepository
    private lateinit var viewModel: GroupsViewModel

    @Before
    fun setup() {
        repository = mockk()
        coEvery { repository.getGroups() } returns flowOf(Resource.Success(emptyList()))
        viewModel = GroupsViewModel(repository)
    }


    @Test
    fun `createGroup with empty name sets error and does not call repository`() = runTest {
        // Act
        viewModel.createGroup("")

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.createGroupError).isEqualTo("Group name cannot be empty")
        coVerify(exactly = 0) { repository.joinGroup(any()) }
    }

    @Test
    fun `createGroup emits Loading then Success`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { repository.getGroups() } returns flowOf(
            Resource.Loading(),
            Resource.Success(emptyList())
        )

        coEvery { repository.createGroup("Test Group") } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )

        viewModel = GroupsViewModel(repository)

        val msgDeferred = async { viewModel.successMessage.first() }
        val closeDeferred = async { viewModel.closeDialogEvent.first() }

        // Act
        viewModel.createGroup("Test Group")
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.createGroupError).isNull()
        assertThat(state.createGroupLoading).isFalse()

        assertThat(msgDeferred.await())
            .isEqualTo("Successfully created group 'Test Group'")
        closeDeferred.await()
    }

    @Test
    fun `joinGroup with empty invitation code sets error`() = runTest {
        // Act
        viewModel.joinGroup("")
        val state = viewModel.uiState.value

        // Assert
        assertThat(state.joinGroupError).isEqualTo("Invitation code cannot be empty")

        coVerify(exactly = 0) { repository.joinGroup(any()) }
    }

    @Test
    fun `joinGroup with short code sets error`() = runTest {
        // Arrange
        viewModel.joinGroup("ABC")
        val state = viewModel.uiState.value

        // Assert
        assertThat(state.joinGroupError)
            .isEqualTo("Invitation code should be 7 characters long")

        coVerify(exactly = 0) { repository.joinGroup(any()) }
    }

    @Test
    fun `joinGroup emits Loading then Success`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { repository.joinGroup("ABCDEFG") } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )
        viewModel = GroupsViewModel(repository)

        val msgDeferred = async { viewModel.successMessage.first() }
        val closeDeferred = async { viewModel.closeDialogEvent.first() }

        // Acct
        viewModel.joinGroup("ABCDEFG")
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.joinGroupLoading).isFalse()
        assertThat(state.joinGroupError).isNull()
        assertThat(msgDeferred.await()).isEqualTo("Successfully joined group")
        closeDeferred.await()
    }
}