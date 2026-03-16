package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.data.repository.ReviewRepository
import com.example.housify.feature.tasks.RatingToDoScreen
import com.example.housify.feature.tasks.RatingToDoViewModel
import com.example.housify.ui.theme.HousifyTheme
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RatingToDoScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: ReviewRepository
    private lateinit var viewModel: RatingToDoViewModel
    private lateinit var rating: UncompletedRating

    @Before
    fun setup() {
        repository = mockk(relaxed = true)

        coEvery { repository.getPeerReviewByReviewId(any()) } returns mockk(relaxed = true)
        coEvery { repository.getDownloadSignedUrl(any()) } returns emptyList()

        viewModel = RatingToDoViewModel(repository)

        rating = UncompletedRating(
            groupId = "group-1",
            reviewId = "review-1",
            reviewCreatedAt = System.currentTimeMillis(),
            revieweeName = "Alice",
            title = "Clean the room",
            groupName = "Group 1",
            place = "Kitchen"
        )
    }

    @Test
    fun clickingRate_callsCreateRating() {
        // Arrange
        composeRule.setContent {
            HousifyTheme {
                RatingToDoScreen(
                    viewModel = viewModel,
                    rating = rating,
                    onBack = {}
                )
            }
        }

        // Act
        composeRule.onAllNodes(hasContentDescription("Star 5"))[0]
            .performClick()

        composeRule.onAllNodes(hasContentDescription("Star 5"))[1]
            .performClick()

        composeRule.onAllNodes(hasSetTextAction())
            .onFirst()
            .performTextInput("Nice work")

        composeRule.onNodeWithText("RATE").performClick()

        composeRule.waitForIdle()

        // Assert
        coVerify {
            repository.createRating(
                groupId = rating.groupId,
                reviewId = rating.reviewId,
                cleanlinessScore = any(),
                punctualityScore = any(),
                comment = "Nice work"
            )
        }
    }
}
