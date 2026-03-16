package com.example.housify

import androidx.lifecycle.SavedStateHandle
import com.example.housify.domain.model.TaskDetails
import com.example.housify.domain.repository_interfaces.TaskRepository
import com.example.housify.feature.groups.tasks.details.TaskDetailsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class TaskDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TaskRepository
    private lateinit var viewModel: TaskDetailsViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeGroupId = "group123"
    private val fakeTaskId = "task456"

    @Before
    fun setup() {
        repository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "groupId" to fakeGroupId,
                "taskId" to fakeTaskId
            )
        )
    }

    @Test
    fun `deleteTask success updates uiState and emits success and back event`() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            val fakeTaskDetails = mockk<TaskDetails>(relaxed = true)
            every { fakeTaskDetails.task.title } returns "Wash dishes"

            coEvery {
                repository.getTaskDetails(fakeTaskId, fakeGroupId)
            } returns flowOf(
                Resource.Success(fakeTaskDetails)
            )

            coEvery {
                repository.deleteTask(fakeTaskId, fakeGroupId)
            } returns flowOf(
                Resource.Loading(),
                Resource.Success(Unit)
            )

            viewModel = TaskDetailsViewModel(repository, savedStateHandle)
            advanceUntilIdle()

            val msgDeferred = async { viewModel.successMessage.first() }
            val backDeferred = async { viewModel.onBackEvent.first() }

            // Act
            viewModel.deleteTask()
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertThat(state.deleteTaskLoading).isFalse()
            assertThat(state.deleteTaskError).isNull()
            assertThat(msgDeferred.await())
                .isEqualTo("Successfully deleted task 'Wash dishes'")
            backDeferred.await()
            coVerify(exactly = 1) { repository.deleteTask(fakeTaskId, fakeGroupId) }
        }

    @Test
    fun `deleteTask error sets error and stops loading`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeTaskDetails = mockk<TaskDetails>(relaxed = true)

            coEvery {
                repository.getTaskDetails(fakeTaskId, fakeGroupId)
            } returns flowOf(
                Resource.Success(fakeTaskDetails)
            )

            coEvery {
                repository.deleteTask(fakeTaskId, fakeGroupId)
            } returns flowOf(
                Resource.Loading(),
                Resource.Error("Delete failed")
            )

            viewModel = TaskDetailsViewModel(repository, savedStateHandle)
            advanceUntilIdle()

            // Act
            viewModel.deleteTask()
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertThat(state.deleteTaskLoading).isFalse()
            assertThat(state.deleteTaskError).isEqualTo("Delete failed")

            coVerify(exactly = 1) { repository.deleteTask(fakeTaskId, fakeGroupId) }
        }
}
