package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.feature.tasks.ImageGallery
import com.example.housify.feature.tasks.RatingComponent
import com.example.housify.ui.theme.HousifyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskToDoUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun ratingComponent_clickStar_updatesCallback() {
        // Arrange
        var lastRating = 0

        // Act
        composeRule.setContent {
            HousifyTheme {
                RatingComponent(
                    title = "Cleanliness",
                    rating = 0,
                    onRatingChanged = { lastRating = it }
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("Cleanliness")
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Star 3")
            .assertIsDisplayed()
            .performClick()

        assertEquals(3, lastRating)
    }

    @Test
    fun imageGallery_empty_showsPlaceholderText() {
        // Act
        composeRule.setContent {
            HousifyTheme {
                ImageGallery(
                    mediaItems = emptyList()
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("No media available")
            .assertIsDisplayed()
    }
}
