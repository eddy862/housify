package com.example.housify

import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.remote.dto.PeerReview
import com.example.housify.data.repository.ReviewRepository
import com.example.housify.feature.tasks.RatingToDoViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RatingToDoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ReviewRepository
    private lateinit var viewModel: RatingToDoViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = RatingToDoViewModel(repository)
    }

    @Test
    fun `createRating calls repository with correct arguments`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            coEvery {
                repository.createRating(
                    groupId = any(),
                    reviewId = any(),
                    cleanlinessScore = any(),
                    punctualityScore = any(),
                    comment = any()
                )
            } returns Unit

            val groupId = "group123"
            val reviewId = "review456"
            val cleanliness = 4
            val punctuality = 5
            val comment = "Nice job"

            // Act
            viewModel.createRating(
                groupId = groupId,
                reviewId = reviewId,
                cleanlinessScore = cleanliness,
                punctualityScore = punctuality,
                comment = comment
            )
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) {
                repository.createRating(
                    groupId = groupId,
                    reviewId = reviewId,
                    cleanlinessScore = cleanliness,
                    punctualityScore = punctuality,
                    comment = comment
                )
            }
        }

    @Test
    fun `getDownloadSignedUrl success updates state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val reviewId = "review123"

            val fakePeerReview = mockk<PeerReview>(relaxed = true)

            val mediaList = listOf(
                MediaItem(fileUrl = "https://example.com/file1.jpg", mimeType = "image/jpeg"),
                MediaItem(fileUrl = "https://example.com/file2.mp4", mimeType = "video/mp4")
            )
            every { fakePeerReview.media } returns mediaList

            val fakeUrls = listOf(
                mockk<DownloadSignedUrl>(relaxed = true),
                mockk<DownloadSignedUrl>(relaxed = true)
            )

            coEvery { repository.getPeerReviewByReviewId(reviewId) } returns fakePeerReview
            coEvery { repository.getDownloadSignedUrl(fakePeerReview.media) } returns fakeUrls

            // Act
            viewModel.getDownloadSignedUrl(reviewId)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.downloadSignedUrl.value).isEqualTo(fakeUrls)

            coVerify(exactly = 1) { repository.getPeerReviewByReviewId(reviewId) }
            coVerify(exactly = 1) { repository.getDownloadSignedUrl(fakePeerReview.media) }
        }

    @Test
    fun `getDownloadSignedUrl error keeps state null`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val reviewId = "review123"

            coEvery { repository.getPeerReviewByReviewId(reviewId) } throws Exception("Network error")

            // Act
            viewModel.getDownloadSignedUrl(reviewId)
            advanceUntilIdle()

            // Assert
            assertThat(viewModel.downloadSignedUrl.value).isNull()

            coVerify(exactly = 1) { repository.getPeerReviewByReviewId(reviewId) }
            coVerify(exactly = 0) { repository.getDownloadSignedUrl(any()) }
        }

}