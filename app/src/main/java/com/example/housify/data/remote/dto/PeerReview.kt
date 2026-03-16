package com.example.housify.data.remote.dto

data class PeerReview(
    val groupId: String,
    val taskId: String,
    val taskInstanceId: String,
    val userId: String,
    val media: List<MediaItem>,
    val description: String,
    val createdAt: Long
)