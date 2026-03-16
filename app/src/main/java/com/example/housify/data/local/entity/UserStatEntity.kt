package com.example.housify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.housify.data.remote.dto.UserStat

@Entity(tableName = "userStats")
data class UserStatEntity (
    @PrimaryKey val userId: String,
    val totalOverdueTasksCount: Int,
    val totalCompletedTasksCount: Int,
    val totalOverdueReviewCount: Int,
    val totalCompletedReviewCount: Int
)

fun UserStatEntity.toDomain(): UserStat {
    return UserStat(
        totalOverdueTasksCount = totalOverdueTasksCount,
        totalCompletedTasksCount = totalCompletedTasksCount,
        totalOverdueReviewCount = totalOverdueReviewCount,
        totalCompletedReviewCount = totalCompletedReviewCount
    )
}