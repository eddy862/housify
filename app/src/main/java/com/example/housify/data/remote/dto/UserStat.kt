package com.example.housify.data.remote.dto

import com.example.housify.data.local.entity.UserStatEntity

data class UserStat(
    val totalOverdueTasksCount: Int,
    val totalCompletedTasksCount: Int,
    val totalOverdueReviewCount: Int,
    val totalCompletedReviewCount: Int
)

fun UserStat.toEntity(userId: String): UserStatEntity {
    return UserStatEntity(
        userId = userId,
        totalOverdueTasksCount = totalOverdueTasksCount,
        totalCompletedTasksCount = totalCompletedTasksCount,
        totalOverdueReviewCount = totalOverdueReviewCount,
        totalCompletedReviewCount = totalCompletedReviewCount
    )
}