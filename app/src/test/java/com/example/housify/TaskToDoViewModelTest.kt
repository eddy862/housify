package com.example.housify

import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.remote.dto.SignedUrlResponse
import com.example.housify.data.remote.dto.UploadMedia
import com.example.housify.data.repository.ReviewRepository
import com.example.housify.feature.tasks.TaskToDoViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskToDoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ReviewRepository
    private lateinit var viewModel: TaskToDoViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = TaskToDoViewModel(repository)
    }

    @Test
    fun `createPeerReview success calls all repository methods and updates state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val groupId = "group123"
            val taskId = "task123"
            val taskInstanceId = "instance123"
            val bytes = byteArrayOf(1, 2, 3)
            val mimeType = "image/jpeg"
            val description = "Nice work"

            val mediaList = listOf(
                UploadMedia(
                    bytes = bytes,
                    mimeType = mimeType
                )
            )

            val fakeSignedUrl = SignedUrlResponse(
                signedUploadUrl = "https://upload.example.com/signed",
                objectPath = "https://storage.example.com/public/image.jpg",
                mimeType = mimeType
            )

            coEvery {
                repository.getUploadSignedUrl(
                    groupId = groupId,
                    taskId = taskId,
                    taskInstanceId = taskInstanceId,
                    mimeType = mimeType
                )
            } returns fakeSignedUrl

            coEvery {
                repository.uploadToGCS(
                    signedUrl = "https://upload.example.com/signed",
                    bytes = bytes,
                    mimeType = mimeType
                )
            } returns true

            coEvery {
                repository.createPeerReview(
                    groupId = groupId,
                    taskId = taskId,
                    taskInstanceId = taskInstanceId,
                    media = any(),
                    description = description
                )
            } returns Unit

            // Act
            viewModel.createPeerReview(
                groupId = groupId,
                taskId = taskId,
                taskInstanceId = taskInstanceId,
                media = mediaList,
                description = description
            )

            advanceUntilIdle()

            // Assert
            val mediaSlot = slot<List<MediaItem>>()

            coVerify(exactly = 1) {
                repository.getUploadSignedUrl(groupId, taskId, taskInstanceId, mimeType)
            }
            coVerify(exactly = 1) {
                repository.uploadToGCS("https://upload.example.com/signed", bytes, mimeType)
            }
            coVerify(exactly = 1) {
                repository.createPeerReview(
                    groupId = groupId,
                    taskId = taskId,
                    taskInstanceId = taskInstanceId,
                    media = capture(mediaSlot),
                    description = description
                )
            }

            val media = mediaSlot.captured
            assertThat(media).hasSize(1)
            assertThat(media[0].fileUrl).isEqualTo("https://storage.example.com/public/image.jpg")
            assertThat(media[0].mimeType).isEqualTo(mimeType)
        }

    @Test
    fun `createPeerReview when uploadToGCS fails does not call createPeerReview`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val groupId = "group123"
            val taskId = "task123"
            val taskInstanceId = "instance123"
            val bytes = byteArrayOf(1, 2, 3)
            val mimeType = "image/jpeg"
            val description = "Nice work"

            val mediaList = listOf(
                UploadMedia(
                    bytes = bytes,
                    mimeType = mimeType
                )
            )

            val fakeSignedUrl = SignedUrlResponse(
                signedUploadUrl = "https://upload.example.com/signed",
                objectPath = "https://storage.example.com/public/image.jpg",
                mimeType = mimeType
            )

            coEvery {
                repository.getUploadSignedUrl(groupId, taskId, taskInstanceId, mimeType)
            } returns fakeSignedUrl

            coEvery {
                repository.uploadToGCS(
                    signedUrl = "https://upload.example.com/signed",
                    bytes = bytes,
                    mimeType = mimeType
                )
            } returns false

            // Act
            viewModel.createPeerReview(
                groupId = groupId,
                taskId = taskId,
                taskInstanceId = taskInstanceId,
                media = mediaList,
                description = description
            )

            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) {
                repository.getUploadSignedUrl(groupId, taskId, taskInstanceId, mimeType)
            }
            coVerify(exactly = 1) {
                repository.uploadToGCS("https://upload.example.com/signed", bytes, mimeType)
            }
            coVerify(exactly = 0) {
                repository.createPeerReview(any(), any(), any(), any(), any())
            }
        }
}