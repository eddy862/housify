package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.data.local.datastore.ThemePreferenceManager
import com.example.housify.data.repository.AuthRepository
import com.example.housify.data.repository.NotificationRepository
import com.example.housify.data.repository.ProfileRepository
import com.example.housify.feature.profile.ProfileScreen
import com.example.housify.feature.auth.AuthViewModel
import com.example.housify.feature.auth.AuthState
import com.example.housify.feature.profile.ProfileViewModel
import com.example.housify.ui.theme.HousifyTheme
import com.example.housify.ui.theme.AppTheme
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun logoutButton_callsSignOut_and_onSuccessLogout() {
        // Arrange
        val authRepository = mockk<AuthRepository>(relaxed = true)
        val notificationRepository = mockk<NotificationRepository>(relaxed = true)
        val profileRepository = mockk<ProfileRepository>(relaxed = true)

        every { authRepository.isUserLoggedIn() } returns false

        val authViewModel = AuthViewModel(authRepository, notificationRepository)
        val profileViewModel = ProfileViewModel(authRepository, profileRepository)

        var logoutNavigated = false

        // Act
        composeRule.setContent {
            HousifyTheme {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onSelectCompletedTasks = {},
                    onSelectCompletedRatings = {},
                    onSuccessLogout = { logoutNavigated = true },
                    currentTheme = AppTheme.SYSTEM,
                    onSwitchTheme = {},
                    onEnterUpdateIpScreen = {}
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("logout")
            .performClick()

        // Assert
        assertThat(logoutNavigated).isTrue()
        coVerify { authRepository.signOut() }
    }

    @Test
    fun clickViewCompletedTasks_triggers_onSelectCompletedTasks() {
        // Arrange
        val authRepository = mockk<AuthRepository>(relaxed = true)
        val notificationRepository = mockk<NotificationRepository>(relaxed = true)
        val profileRepository = mockk<ProfileRepository>(relaxed = true)

        every { authRepository.isUserLoggedIn() } returns false

        val authViewModel = AuthViewModel(authRepository, notificationRepository)
        val profileViewModel = ProfileViewModel(authRepository, profileRepository)

        var clicked = false

        // Act
        composeRule.setContent {
            HousifyTheme {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onSelectCompletedTasks = { clicked = true },
                    onSelectCompletedRatings = {},
                    onSuccessLogout = {},
                    currentTheme = AppTheme.SYSTEM,
                    onSwitchTheme = {},
                    onEnterUpdateIpScreen = {}
                )
            }
        }

        composeRule
            .onNodeWithText("View completed tasks >")
            .performClick()

        // Assert
        assertThat(clicked).isTrue()
    }

    @Test
    fun completedRatingsSection_isDisplayed() {
        // Arrange
        val authRepository = mockk<AuthRepository>(relaxed = true)
        val notificationRepository = mockk<NotificationRepository>(relaxed = true)
        val profileRepository = mockk<ProfileRepository>(relaxed = true)

        every { authRepository.isUserLoggedIn() } returns false

        val authViewModel = AuthViewModel(authRepository, notificationRepository)
        val profileViewModel = ProfileViewModel(authRepository, profileRepository)

        // Act
        composeRule.setContent {
            HousifyTheme {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onSelectCompletedTasks = {},
                    onSelectCompletedRatings = {},
                    onSuccessLogout = {},
                    currentTheme = AppTheme.SYSTEM,
                    onSwitchTheme = {},
                    onEnterUpdateIpScreen = {}
                )
            }
        }

        // Assert
        composeRule.onNodeWithText("Completed Ratings")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Total")
            .assertIsDisplayed()
    }
}
