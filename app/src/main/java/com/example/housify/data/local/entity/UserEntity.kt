package com.example.housify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.housify.data.remote.dto.UserDetailsResponse

@Entity(tableName = "users")
data class UserEntity (
    @PrimaryKey val userId: String,
    val username: String,
    val email: String,
    val profileUrl: String?,
    val groupIds: List<String>?,
    val pastGroupIds: List<String>?,
    val createdAt: Long
)

fun UserEntity.toDomain(): UserDetailsResponse {
    return UserDetailsResponse(
        uid = userId,
        username = username,
        email = email,
        profileUrl = profileUrl,
        groupIds = groupIds,
        pastGroupIds = pastGroupIds,
        createdAt = createdAt
    )
}