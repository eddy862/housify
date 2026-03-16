package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompletedTask(
    val title: String,
    val groupName: String,
    val place: String,
    val description: String,
    val media: List<MediaItem>,
    val createdAt: Long,
    val ratings: List<RatingScore>
)