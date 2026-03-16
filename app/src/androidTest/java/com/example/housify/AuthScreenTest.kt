package com.example.housify

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.housify.feature.auth.AuthScreen
import com.example.housify.feature.auth.AuthViewModel
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.housify.data.remote.dto.RegisterResponse
import com.example.housify.data.repository.AuthRepository
import com.example.housify.data.repository.NotificationRepository
import com.example.housify.feature.auth.AuthState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: AuthRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        repository = mockk()
        notificationRepository = mockk(relaxed = true)
        viewModel = AuthViewModel(repository, notificationRepository)
    }

    @Test
    fun signup_with_empty_fields_shows_profile_name_error() {
        // Arrange
        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onSuccessLogin = {}
            )
        }

        // Act
        composeRule.onNodeWithText("SIGNUP").performClick()
        composeRule.onNodeWithText("SIGN UP").performClick()

        // Assert
        composeRule.onNodeWithText("Profile name cannot be empty")
            .assertIsDisplayed()
    }

    @Test
    fun signup_success_shows_registered_successfully() {
        // Arrange
        val fakeResponse = mockk<RegisterResponse>(relaxed = true)

        coEvery {
            repository.register(
                email = "newuser@mail.com",
                password = "123456",
                username = "New User"
            )
        } returns fakeResponse

        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onSuccessLogin = {}
            )
        }

        // Act
        composeRule.onNodeWithText("SIGNUP").performClick()
        composeRule.onNodeWithText("Profile name")
            .performTextInput("New User")
        composeRule.onNodeWithText("Email address")
            .performTextInput("newuser@mail.com")
        composeRule.onNodeWithText("Password")
            .performTextInput("123456")
        composeRule.onNodeWithText("Confirm password")
            .performTextInput("123456")
        composeRule.onNodeWithText("SIGN UP").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.authState.value is AuthState.RegisterSuccess
        }

        // Assert
        composeRule.onNodeWithText("Registered successfully")
            .assertIsDisplayed()
    }

    @Test
    fun loginSuccess_triggers_onSuccessLogin() {
        // Arrange
        val fakeUser = mockk<FirebaseUser>(relaxed = true)
        every { fakeUser.email } returns "test@gmail.com"

        coEvery {
            repository.signInWithEmail("test@gmail.com", "123456")
        } returns Result.success(fakeUser)

        var navigated = false

        composeRule.setContent {
            AuthScreen(
                viewModel = viewModel,
                onSuccessLogin = { navigated = true }
            )
        }

        // Act
        composeRule.onNodeWithText("abc@email.com")
            .performTextInput("test@gmail.com")
        composeRule.onNodeWithText("Password")
            .performTextInput("123456")
        composeRule.onNodeWithText("LOG IN")
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            navigated
        }

        // Assert
        assertThat(navigated).isTrue()
    }

}