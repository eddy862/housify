package com.example.housify.data.remote.dto

import com.example.housify.data.local.entity.RatingEntity
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
data class UncompletedRating(
    val reviewId: String,
    val groupId: String,
    val groupName: String,
    val revieweeName: String,
    val title: String,
    val place: String,
    val reviewCreatedAt: Long
)

fun UncompletedRating.toEntity(userId: String): RatingEntity {
    return RatingEntity(
        reviewId = reviewId,
        userId = userId,
        groupId = groupId,
        groupName = groupName,
        revieweeName = revieweeName,
        title = title,
        place = place,
        reviewCreatedAt = reviewCreatedAt
    )
}