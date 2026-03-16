package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.housify.ui.theme.HousifyTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.domain.model.Group
import com.example.housify.domain.model.User
import com.example.housify.feature.groups.groups_list.GroupEntryCard
import com.example.housify.feature.groups.groups_list.TopIconButton

@RunWith(AndroidJUnit4::class)
class GroupScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun groupEntryCard_click_calls_onClickWithGroupId() {
        // Arrange
        var clickedId: String? = null

        val adminUser = User(
            id = "admin1",
            name = "Admin User",
            isCurrentUser = true
        )
        val member1 = User(
            id = "member1",
            name = "Member One",
            isCurrentUser = false
        )

        val fakeGroup = Group(
            id = "group123",
            name = "My Test Group",
            numberOfAssignedTasks = 3,
            admin = adminUser,
            members = listOf(adminUser, member1),
            createdAt = 0L,
            invitationCode = "ABC1234",
            isUserAdmin = true
        )

        // Act
        composeRule.setContent {
            HousifyTheme(dynamicColor = false) {
                GroupEntryCard(
                    onClick = { id -> clickedId = id },
                    group = fakeGroup
                )
            }
        }

        composeRule.onNodeWithText("My Test Group")
            .performClick()

        // Assert
        assertThat(clickedId).isEqualTo("group123")
    }

    @Test
    fun topIconButton_enabled_calls_onClick() {
        // Arrange
        var clicked = false

        // Act
        composeRule.setContent {
            HousifyTheme(dynamicColor = false) {
                TopIconButton(
                    onClick = { clicked = true },
                    resId = R.drawable.create_group,
                    contentDesc = "create group",
                    enabled = true
                )
            }
        }

        val node = composeRule.onNodeWithContentDescription("create group")
        node.assertIsEnabled()
        node.performClick()

        // Assert
        assertThat(clicked).isTrue()
    }

    @Test
    fun topIconButton_disabled_doesNotCallOnClick() {
        // Arrange
        var clicked = false

        // Act
        composeRule.setContent {
            HousifyTheme(dynamicColor = false) {
                TopIconButton(
                    onClick = { clicked = true },
                    resId = R.drawable.create_group,
                    contentDesc = "create group",
                    enabled = false
                )
            }
        }

        val node = composeRule.onNodeWithContentDescription("create group")
        node.assertIsNotEnabled()
        node.performClick()

        // Assert
        assertThat(clicked).isFalse()
    }

}