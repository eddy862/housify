package com.example.housify

import com.example.housify.data.remote.dto.FutureTask
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.data.repository.ReviewRepository
import com.example.housify.data.repository.TaskRepositoryImpl
import com.example.housify.feature.tasks.TasksViewModel
import com.google.common.base.Verify.verify
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var taskRepository: TaskRepositoryImpl
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setup() {
        taskRepository = mockk()
        reviewRepository = mockk()
        viewModel = TasksViewModel(taskRepository, reviewRepository)
    }

    @Test
    fun `setSelectedDate updates selectedDate state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Act
            viewModel.setSelectedDate("2025-01-15")

            // Assert
            assertThat(viewModel.selectedDate.value).isEqualTo("2025-01-15")
        }

    @Test
    fun `getTodayTasks sets todayTasks and clears futureTasks`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeTodayTasks = listOf(
                mockk<TodayTask>(relaxed = true),
                mockk<TodayTask>(relaxed = true)
            )

            every { taskRepository.getTodayTasks() } returns flowOf(
                Resource.Success(fakeTodayTasks)
            )

            // Act
            viewModel.getTodayTasks()
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.todayTasks.value).isEqualTo(fakeTodayTasks)
            assertThat(viewModel.futureTasks.value).isEmpty()
            verify(exactly = 1) { taskRepository.getTodayTasks() }
        }

    @Test
    fun `getFutureTasks sets futureTasks and clears todayTasks`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeDate = "2025-12-31"

            val fakeFutureTasks = listOf(
                mockk<FutureTask>(relaxed = true),
                mockk<FutureTask>(relaxed = true)
            )

            coEvery { taskRepository.getFutureTasks(fakeDate) } returns fakeFutureTasks

            // Act
            viewModel.getFutureTasks(fakeDate)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.futureTasks.value).isEqualTo(fakeFutureTasks)
            assertThat(viewModel.todayTasks.value).isEmpty()

            coVerify(exactly = 1) { taskRepository.getFutureTasks(fakeDate) }
        }

    @Test
    fun `getUncompletedTask sets ratings list`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeRatings = listOf(
                mockk<UncompletedRating>(relaxed = true),
                mockk<UncompletedRating>(relaxed = true)
            )

            every { reviewRepository.getUncompletedTask() } returns flowOf(
                Resource.Success(fakeRatings)
            )

            // Act
            viewModel.getUncompletedTask()
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.ratings.value).isEqualTo(fakeRatings)
            verify(exactly = 1) { reviewRepository.getUncompletedTask() }
        }

    @Test
    fun `getUncompletedTask when repository returns error keeps ratings empty`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            every { reviewRepository.getUncompletedTask() } returns flowOf(
                Resource.Loading(),
                Resource.Error("Network error", data = emptyList())
            )

            // Act
            viewModel.getUncompletedTask()
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.ratings.value).isEmpty()
            verify(exactly = 1) { reviewRepository.getUncompletedTask() }
        }
}