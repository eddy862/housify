package com.example.housify

import androidx.lifecycle.SavedStateHandle
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import com.example.housify.feature.groups.leaderboard.history_entry.LeaderboardHistoryEntryViewModel
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
class LeaderboardHistoryEntryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: LeaderboardRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val fakeGroupId = "group123"
    private val fakeWeek = 5

    @Before
    fun setup() {
        repository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "groupId" to fakeGroupId,
                "week" to fakeWeek
            )
        )
    }

    @Test
    fun `init loads leaderboard successfully`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakeLeaderboard = Leaderboard(
            id = "lb1",
            groupId = fakeGroupId,
            week = fakeWeek,
            startDate = "",
            endDate = "",
            entries = emptyList()
        )

        coEvery {
            repository.getLeaderboardByWeek(fakeGroupId, fakeWeek.toLong())
        } returns flowOf(
            Resource.Loading(),
            Resource.Success(fakeLeaderboard)
        )

        // Act
        val viewModel = LeaderboardHistoryEntryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.leaderboard).isEqualTo(fakeLeaderboard)
    }

    @Test
    fun `init handles error from repository`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        coEvery {
            repository.getLeaderboardByWeek(fakeGroupId, fakeWeek.toLong())
        } returns flowOf(
            Resource.Loading(),
            Resource.Error("Network error")
        )

        // Act
        val viewModel = LeaderboardHistoryEntryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.leaderboard).isNull()
        assertThat(state.error).isEqualTo("Network error")
    }

}