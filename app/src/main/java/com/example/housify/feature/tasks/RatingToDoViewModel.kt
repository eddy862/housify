package com.example.housify.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.remote.dto.PeerReview
import com.example.housify.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RatingToDoViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {
    private val _downloadSignedUrl = MutableStateFlow<List<DownloadSignedUrl>?>(null)
    val downloadSignedUrl = _downloadSignedUrl

    suspend fun createRating(
        groupId: String,
        reviewId: String,
        cleanlinessScore: Int,
        punctualityScore: Int,
        comment: String
    ): Boolean {
        return try {
            repository.createRating(
                groupId = groupId,
                reviewId = reviewId,
                cleanlinessScore = cleanlinessScore,
                punctualityScore = punctualityScore,
                comment = comment
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

//    fun getPeerReviewByReviewId(
//        reviewId: String
//    ) {
//        viewModelScope.launch {
//            try {
//                val result = repository.getPeerReviewByReviewId(reviewId)
//                _peerReview.value = result
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // TODO: expose error state for UI
//            }
//        }
//    }

    fun getDownloadSignedUrl(
        reviewId: String
    ) {
        viewModelScope.launch {
            try {
                val peerReview = repository.getPeerReviewByReviewId(reviewId)

                val downloadSignedUrl = repository.getDownloadSignedUrl(peerReview.media)
                _downloadSignedUrl.value = downloadSignedUrl
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: expose error state for UI
            }
        }
    }
}