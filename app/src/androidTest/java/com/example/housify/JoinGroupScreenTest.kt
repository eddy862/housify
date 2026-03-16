package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.feature.joinGroup.JoinGroupContent
import com.example.housify.feature.joinGroup.JoinGroupUiState
import com.example.housify.ui.theme.HousifyTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JoinGroupScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun joinGroupContent_showsGroupName_andButtonsTriggerCallbacks() {
        // Arrange
        var joinClicked = false
        var backClicked = false

        val uiState = JoinGroupUiState(
            groupName = "My Test Group",
            isLoading = false,
            error = null,
            isJoinGroupLoading = false
        )

        // Act
        composeRule.setContent {
            HousifyTheme {
                JoinGroupContent(
                    uiState = uiState,
                    onBack = { backClicked = true },
                    onJoinGroup = { joinClicked = true }
                )
            }
        }

        // Assert
        composeRule
            .onNodeWithText("Are you sure you want to join the group 'My Test Group'")
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Join")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Join").performClick()
        assertThat(joinClicked).isTrue()
        composeRule.onNodeWithText("Cancel").performClick()
        assertThat(backClicked).isTrue()
    }
}
