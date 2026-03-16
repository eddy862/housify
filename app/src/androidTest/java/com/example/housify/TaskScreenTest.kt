package com.example.housify

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.housify.feature.tasks.CustomTab
import com.example.housify.feature.tasks.DateSelector
import com.example.housify.feature.tasks.TaskTab
import com.example.housify.ui.theme.HousifyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun taskTab_click_triggersCallback() {
        // Arrange
        var clicked = false

        // Act
        composeRule.setContent {
            HousifyTheme {
                TaskTab(
                    title = "Clean the basin",
                    location = "Kitchen",
                    clockOrPersonIcon = R.drawable.access_time,
                    timeOrPerson = "Today 3pm",
                    onClick = { clicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Clean the basin")
            .assertIsDisplayed()
            .performClick()

        // Assert
        assertTrue(clicked)
    }

    @Test
    fun customTab_shows_correct_counts() {
        // Act
        composeRule.setContent {
            HousifyTheme {
                CustomTab(
                    time = "10 Jan 2026, Mon",
                    tasks = "3",
                    ratings = "2"
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("3").assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()

        composeRule.onNodeWithText("Tasks to be completed").assertIsDisplayed()
        composeRule.onNodeWithText("Ratings to be completed").assertIsDisplayed()
    }


    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    @Test
    fun dateSelector_clicks_onDateSelectedCalled() {
        // Arrange
        var lastSelectedLabel: String? = null
        val startDate = LocalDate.of(2025, 1, 1)

        // Act
        composeRule.setContent {
            HousifyTheme {
                DateSelector(
                    weekStartDate = startDate,
                    selectedDate = startDate,
                    onDateSelected = { date ->
                        lastSelectedLabel = "${date.dayOfWeek.name.take(3)} ${date.dayOfMonth}"
                    }
                )
            }
        }

        composeRule
            .onNodeWithText("1")
            .performClick()

        // Assert
        assertTrue("onDateSelected should have been called", lastSelectedLabel != null)
    }
}