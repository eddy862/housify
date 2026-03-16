package com.example.housify.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.housify.data.remote.dto.UncompletedRating

@Entity(
    tableName = "ratings",
    indices = [Index(value = ["userId"])]
)
data class RatingEntity(
    @PrimaryKey val reviewId: String,
    val userId: String,
    val groupId: String,
    val groupName: String,
    val revieweeName: String,
    val title: String,
    val place: String,
    val reviewCreatedAt: Long
)

fun RatingEntity.toDomain(): UncompletedRating {
    return UncompletedRating(
        reviewId = reviewId,
        groupId = groupId,
        groupName = groupName,
        revieweeName = revieweeName,
        title = title,
        place = place,
        reviewCreatedAt = reviewCreatedAt
    )
}
