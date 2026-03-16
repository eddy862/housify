package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import com.example.housify.ui.theme.HousifyTheme
import com.example.housify.Resource
import com.example.housify.feature.groups.leaderboard.history_list.LeaderboardHistoryScreen
import com.example.housify.feature.groups.leaderboard.history_list.LeaderboardHistoryViewModel
import com.example.housify.domain.model.LeaderboardHistory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LeaderboardHistoryScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun leaderboardHistoryScreen_showsList_andCallsCallbackOnClick() {
        // Arrange
        val fakeHistoryList = listOf(
            LeaderboardHistory(
                id = "lh1",
                groupId = "group123",
                startDate = "2024-01-01",
                endDate = "2024-01-07",
                week = 1
            ),
            LeaderboardHistory(
                id = "lh2",
                groupId = "group123",
                startDate = "2024-01-08",
                endDate = "2024-01-14",
                week = 2
            )
        )

        val repository = mockk<LeaderboardRepository>()
        every { repository.getAllLeaderboards("group123") } returns
                flowOf(Resource.Success(fakeHistoryList))

        val savedStateHandle = SavedStateHandle(
            mapOf("groupId" to "group123")
        )

        val viewModel = LeaderboardHistoryViewModel(
            leaderboardRepository = repository,
            savedStateHandle = savedStateHandle
        )

        var clickedWeek: Int? = null

        // Act
        composeRule.setContent {
            HousifyTheme {
                LeaderboardHistoryScreen(
                    onSelectLeaderboardHistoryEntry = { week ->
                        clickedWeek = week
                    },
                    viewModel = viewModel
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("Week 1")
            .assertIsDisplayed()
        composeRule.onNodeWithText("2024-01-01 - 2024-01-07")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Week 1")
            .performClick()
        assert(clickedWeek == 1)
    }
}
