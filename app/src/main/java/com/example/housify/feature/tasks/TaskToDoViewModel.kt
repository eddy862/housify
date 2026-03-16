package com.example.housify.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.remote.dto.SignedUrlResponse
import com.example.housify.data.remote.dto.UploadMedia
import com.example.housify.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TaskToDoViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {
    private val _signedUrlState = MutableStateFlow<SignedUrlResponse?>(null)
    val signedUrlState = _signedUrlState

    suspend fun createPeerReview(
        groupId: String,
        taskId: String,
        taskInstanceId: String,
        media: List<UploadMedia>,
        description: String
    ): Boolean {
        return try {
            val mediaItems = mutableListOf<MediaItem>()

            for (m in media) {
                // 1) Signed URL for this file type
                val signedUrlResponse = repository.getUploadSignedUrl(
                    groupId = groupId,
                    taskId = taskId,
                    taskInstanceId = taskInstanceId,
                    mimeType = m.mimeType
                ) ?: throw Exception("Failed to fetch signed URL")

                // 2) Upload file
                val uploadSuccess = repository.uploadToGCS(
                    signedUrl = signedUrlResponse.signedUploadUrl,
                    bytes = m.bytes,
                    mimeType = m.mimeType
                )
                if (!uploadSuccess) throw Exception("GCS upload failed")

                // 3) Add to list that backend expects
                mediaItems += MediaItem(
                    fileUrl = signedUrlResponse.objectPath,
                    mimeType = signedUrlResponse.mimeType
                )
            }

            // 4) ONE peer review with all media (photos + video)
            repository.createPeerReview(
                groupId = groupId,
                taskId = taskId,
                taskInstanceId = taskInstanceId,
                media = mediaItems,
                description = description
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: expose error state
            false
        }
    }
}