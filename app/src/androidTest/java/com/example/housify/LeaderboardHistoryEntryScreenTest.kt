package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.model.LeaderboardEntry
import com.example.housify.domain.model.User
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import com.example.housify.Resource
import com.example.housify.feature.groups.leaderboard.history_entry.LeaderboardHistoryEntryScreen
import com.example.housify.feature.groups.leaderboard.history_entry.LeaderboardHistoryEntryViewModel
import com.example.housify.ui.theme.HousifyTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LeaderboardHistoryEntryScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun leaderboardHistoryEntryScreen_showsWeekAndEntries_onSuccess() {
        // Arrange
        val userAlice = User(
            id = "u1",
            name = "Alice",
            isCurrentUser = false
        )

        val userBob = User(
            id = "u2",
            name = "Bob",
            isCurrentUser = false
        )

        val entries = listOf(
            LeaderboardEntry(user = userAlice, rating = 4.5f),
            LeaderboardEntry(user = userBob, rating = 3.8f),
        )

        val fakeLeaderboard = Leaderboard(
            id = "lb1",
            groupId = "group123",
            startDate = "2024-01-01",
            endDate = "2024-01-07",
            entries = entries,
            week = 3
        )

        val repository = mockk<LeaderboardRepository>()
        every { repository.getLeaderboardByWeek(any(), any()) } returns
                flowOf(Resource.Success(fakeLeaderboard))

        val savedStateHandle = SavedStateHandle(
            mapOf(
                "groupId" to "group123",
                "week" to 3,
            )
        )

        val viewModel = LeaderboardHistoryEntryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        // Act
        composeRule.setContent {
            HousifyTheme {
                LeaderboardHistoryEntryScreen(viewModel = viewModel)
            }
        }

        // Assert
        composeRule.onNodeWithText("Week 3")
            .assertIsDisplayed()
        composeRule.onNodeWithText("(2024-01-01 - 2024-01-07)")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Alice")
            .assertIsDisplayed()
    }

}