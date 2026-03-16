package com.example.housify

import androidx.lifecycle.SavedStateHandle
import com.example.housify.domain.model.LeaderboardHistory
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import com.example.housify.feature.groups.leaderboard.history_list.LeaderboardHistoryViewModel
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
class LeaderboardHistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: LeaderboardRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeGroupId = "group123"

    @Before
    fun setup() {
        repository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "groupId" to fakeGroupId
            )
        )
    }

    @Test
    fun `init loads leaderboard history successfully`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakeHistory1 = mockk<LeaderboardHistory>()
        val fakeHistory2 = mockk<LeaderboardHistory>()
        val fakeList = listOf(fakeHistory1, fakeHistory2)

        coEvery { repository.getAllLeaderboards(fakeGroupId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(fakeList)
        )

        // Act
        val viewModel = LeaderboardHistoryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.leaderboardHistory).isEqualTo(fakeList)
    }

    @Test
    fun `init handles error from repository`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery { repository.getAllLeaderboards(fakeGroupId) } returns flowOf(
            Resource.Loading(),
            Resource.Error("Network error")
        )

        // Act
        val viewModel = LeaderboardHistoryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.leaderboardHistory).isEmpty()
        assertThat(state.error).isEqualTo("Network error")
    }

    @Test
    fun `refreshData re-fetches and updates leaderboard history`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val initialList = listOf(mockk<LeaderboardHistory>())
        val refreshedList = listOf(mockk<LeaderboardHistory>(), mockk())

        coEvery { repository.getAllLeaderboards(fakeGroupId) } returnsMany listOf(
            flowOf(Resource.Success(initialList)),
            flowOf(Resource.Success(refreshedList))
        )

        val viewModel = LeaderboardHistoryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()
        assertThat(viewModel.uiState.value.leaderboardHistory).isEqualTo(initialList)

        // Act
        viewModel.refreshData()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.leaderboardHistory).isEqualTo(refreshedList)
    }

}