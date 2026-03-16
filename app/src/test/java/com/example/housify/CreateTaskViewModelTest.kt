package com.example.housify


import androidx.lifecycle.SavedStateHandle
import com.example.housify.domain.model.PredefinedSpace
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.User
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.domain.repository_interfaces.TaskRepository
import com.example.housify.feature.groups.tasks.create.CreateTaskViewModel
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
class CreateTaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var taskRepository: TaskRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeGroupId = "group123"

    @Before
    fun setup() {
        taskRepository = mockk()
        groupRepository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "groupId" to fakeGroupId
            )
        )
    }

    @Test
    fun `createTask with past date sets error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            coEvery { groupRepository.getGroupAllUsers(fakeGroupId) } returns flowOf(
                Resource.Success(emptyList())
            )

            val viewModel = CreateTaskViewModel(
                taskRepository = taskRepository,
                groupRepository = groupRepository,
                savedStateHandle = savedStateHandle
            )

            advanceUntilIdle()

            val pastTime = 1000L
            val assignees = listOf(User(id = "u1", name = "Alice"))
            val space = PredefinedSpace.Bathroom

            // Act
            viewModel.createTask(
                title = "Clean room",
                assignees = assignees,
                space = space,
                repeatSetting = Repeat.NONE,
                rotationEnabled = false,
                startFrom = pastTime
            )

            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState
            assertThat(state.createTaskError).isEqualTo("Start date must be in the future.")

            coVerify(exactly = 0) {
                taskRepository.createTask(any(), any(), any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `createTask with no assignees sets error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            coEvery { groupRepository.getGroupAllUsers(fakeGroupId) } returns flowOf(
                Resource.Success(emptyList())
            )

            val viewModel = CreateTaskViewModel(
                taskRepository = taskRepository,
                groupRepository = groupRepository,
                savedStateHandle = savedStateHandle
            )

            advanceUntilIdle()

            val futureTime = System.currentTimeMillis() + 60_000
            val space = PredefinedSpace.Bathroom

            // Act
            viewModel.createTask(
                title = "Clean room",
                assignees = emptyList(),
                space = space,
                repeatSetting = Repeat.NONE,
                rotationEnabled = false,
                startFrom = futureTime
            )

            advanceUntilIdle()

            // Act
            val state = viewModel.uiState
            assertThat(state.createTaskLoading).isFalse()
            assertThat(state.createTaskError)
                .isEqualTo("Task must have at least one assignee.")

            coVerify(exactly = 0) {
                taskRepository.createTask(any(), any(), any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `createTask happy path emits success and back event`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            coEvery { groupRepository.getGroupAllUsers(fakeGroupId) } returns flowOf(
                Resource.Success(emptyList())
            )

            coEvery {
                taskRepository.createTask(
                    groupId = any(),
                    title = any(),
                    place = any(),
                    assigneeIds = any(),
                    startDate = any(),
                    isRotational = any(),
                    repeat = any()
                )
            } returns flowOf(
                Resource.Loading(),
                Resource.Success(Unit)
            )

            val viewModel = CreateTaskViewModel(
                taskRepository = taskRepository,
                groupRepository = groupRepository,
                savedStateHandle = savedStateHandle
            )

            advanceUntilIdle()
            val futureTime = System.currentTimeMillis() + 60_000
            val assignees = listOf(
                User(id = "u1", name = "Alice"),
                User(id = "u2", name = "Bob")
            )
            val space = PredefinedSpace.Bathroom

            val msgDeferred = async { viewModel.successMessage.first() }
            val backDeferred = async { viewModel.onBackEvent.first() }

            // Act
            viewModel.createTask(
                title = "Clean room",
                assignees = assignees,
                space = space,
                repeatSetting = Repeat.DAILY,
                rotationEnabled = true,
                startFrom = futureTime
            )

            advanceUntilIdle()

            // Assert
            assertThat(msgDeferred.await()).isEqualTo("Task created successfully")
            backDeferred.await()

            coVerify(exactly = 1) {
                taskRepository.createTask(
                    groupId = fakeGroupId,
                    title = "Clean room",
                    place = space.name,
                    assigneeIds = assignees.map { it.id },
                    startDate = futureTime,
                    isRotational = true,
                    repeat = Repeat.DAILY
                )
            }
        }
}
