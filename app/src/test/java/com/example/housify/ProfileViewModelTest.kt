package com.example.housify

import com.example.housify.data.remote.dto.UserDetailsResponse
import com.example.housify.data.remote.dto.UserStat
import com.example.housify.data.repository.AuthRepository
import com.example.housify.data.repository.ProfileRepository
import com.example.housify.feature.profile.ProfileViewModel
import com.google.common.base.Verify.verify
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var profileRepository: ProfileRepository

    @Before
    fun setup() {
        authRepository = mockk()
        profileRepository = mockk()
    }

    @Test
    fun `init when user logged in fetches user and updates state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeUser = mockk<UserDetailsResponse>(relaxed = true)
            val fakeStat = mockk<UserStat>(relaxed = true)

            every { authRepository.isUserLoggedIn() } returns true
            every { authRepository.getUserFromApi() } returns flowOf(
                Resource.Success(fakeUser)
            )
            every { profileRepository.getUserStat() } returns flowOf(
                Resource.Success(fakeStat)
            )

            // Act
            val viewModel = ProfileViewModel(authRepository, profileRepository)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.user.value).isEqualTo(fakeUser)
            verify(exactly = 1) { authRepository.getUserFromApi() }
        }

    @Test
    fun `init when user not logged in does not fetch user`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            every { authRepository.isUserLoggedIn() } returns false

            // Act
            val viewModel = ProfileViewModel(authRepository, profileRepository)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.user.value).isNull()
            coVerify(exactly = 0) { authRepository.getUserFromApi() }
        }

    @Test
    fun `getUserStat success updates userStat`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            every { authRepository.isUserLoggedIn() } returns false

            val fakeStat: UserStat = mockk(relaxed = true)

            every { profileRepository.getUserStat() } returns flowOf(
                Resource.Success(fakeStat)
            )

            val viewModel = ProfileViewModel(authRepository, profileRepository)
            advanceUntilIdle()

            // Act
            viewModel.getUserStat()
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.userStat.value).isEqualTo(fakeStat)
            verify(exactly = 1) { profileRepository.getUserStat() }
        }

    @Test
    fun `getUserStat error keeps userStat null`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            every { authRepository.isUserLoggedIn() } returns false

            every { profileRepository.getUserStat() } returns flowOf(
                Resource.Loading(),
                Resource.Error("Network error")
            )

            val viewModel = ProfileViewModel(authRepository, profileRepository)
            advanceUntilIdle()

            // Act
            viewModel.getUserStat()
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.userStat.value).isNull()
            verify(exactly = 1) { profileRepository.getUserStat() }
        }

}