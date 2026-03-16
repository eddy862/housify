package com.example.housify

import com.example.housify.data.remote.dto.RegisterResponse
import com.example.housify.data.repository.AuthRepository
import com.example.housify.data.repository.NotificationRepository
import com.example.housify.feature.auth.AuthState
import com.example.housify.feature.auth.AuthViewModel
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        repository = mockk()
        notificationRepository = mockk(relaxed = true)

        viewModel = spyk(AuthViewModel(repository, notificationRepository))

        every { viewModel.sendDeviceTokenAfterLogin() } returns Unit
    }

    @Test
    fun `login emits Loading then Success`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val fakeUser = mockk<FirebaseUser>(relaxed = true)
        every { fakeUser.email } returns "test@gmail.com"

        coEvery { repository.signInWithEmail("test@gmail.com", "123456") } returns
                Result.success(fakeUser)

        // Act
        viewModel.login("test@gmail.com", "123456")

        advanceUntilIdle()

        // Assert
        val state = viewModel.authState.value
        assertThat(state).isInstanceOf(AuthState.LoginSuccess::class.java)
        assertThat((state as AuthState.LoginSuccess).user.email).isEqualTo("test@gmail.com")
    }

    @Test
    fun `register emits Loading then RegisterSuccess`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeResponse = RegisterResponse(
                customToken = "123456789"
            )

            coEvery {
                repository.register(
                    email = "test@gmail.com",
                    password = "123456",
                    username = "TestUser"
                )
            } returns fakeResponse

            // Act
            viewModel.register("TestUser", "test@gmail.com", "123456")
            advanceUntilIdle()

            // Assert
            val state = viewModel.authState.value
            assertThat(state).isInstanceOf(AuthState.RegisterSuccess::class.java)
            assertThat((state as AuthState.RegisterSuccess).response).isEqualTo(fakeResponse)
        }

    @Test
    fun `register emits Error when repository throws`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            coEvery {
                repository.register(any(), any(), any())
            } throws RuntimeException("Something went wrong")

            // Act
            viewModel.register("User", "email@test.com", "pass")
            advanceUntilIdle()

            // Assert
            val state = viewModel.authState.value
            assertThat(state).isInstanceOf(AuthState.Error::class.java)
            assertThat((state as AuthState.Error).message).contains("Something went wrong")
        }

    @Test
    fun `signOut calls repository signOut`() = runTest {
        // Arrange
        coEvery { repository.signOut() } just Runs

        // Act
        viewModel.signOut()

        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.signOut() }
    }
}
