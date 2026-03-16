package com.example.housify

import androidx.lifecycle.SavedStateHandle
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.feature.joinGroup.JoinGroupViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinGroupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: GroupRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: JoinGroupViewModel

    private val invitationCode = "ABCDEF1"

    @Before
    fun setup() {
        repository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf("invitationCode" to invitationCode)
        )
    }

    @Test
    fun `joinGroup success updates uiState and emits success and back event`() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            coEvery {
                repository.getGroupNameByInvitationCode(invitationCode)
            } returns flowOf(
                Resource.Success("My Group")
            )

            coEvery {
                repository.joinGroup(invitationCode)
            } returns flowOf(
                Resource.Loading(),
                Resource.Success(Unit)
            )

            viewModel = JoinGroupViewModel(repository, savedStateHandle)
            advanceUntilIdle()

            val msgDeferred = async { viewModel.successMessage.first() }
            val backDeferred = async { viewModel.onBackEvent.first() }

            // Act
            viewModel.joinGroup()
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertThat(state.isJoinGroupLoading).isFalse()
            assertThat(state.joinGroupError).isNull()
            assertThat(msgDeferred.await())
                .isEqualTo("Successfully join group 'My Group'")
            backDeferred.await()
            coVerify(exactly = 1) { repository.joinGroup(invitationCode) }
        }

    @Test
    fun `joinGroup error sets joinGroupError and stops loading`() =
        runTest(mainDispatcherRule.testDispatcher) {

            // Arrange
            coEvery {
                repository.getGroupNameByInvitationCode(invitationCode)
            } returns flowOf(Resource.Success("My Group"))

            coEvery {
                repository.joinGroup(invitationCode)
            } returns flowOf(
                Resource.Loading(),
                Resource.Error("Invalid code")
            )

            viewModel = JoinGroupViewModel(repository, savedStateHandle)
            advanceUntilIdle()

            // Act
            viewModel.joinGroup()
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertThat(state.isJoinGroupLoading).isFalse()
            assertThat(state.joinGroupError).isEqualTo("Invalid code")

            coVerify(exactly = 1) { repository.joinGroup(invitationCode) }
        }

}