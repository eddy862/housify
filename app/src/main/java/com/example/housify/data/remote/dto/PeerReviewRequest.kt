package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PeerReviewRequest(
    val groupId: String,
    val taskId: String,
    val taskInstanceId: String,
    val media: List<MediaItem>,
    val description: String
)