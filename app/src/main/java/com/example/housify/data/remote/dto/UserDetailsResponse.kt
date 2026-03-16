package com.example.housify.data.remote.dto

import com.example.housify.data.local.entity.UserEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetailsResponse(
    val uid: String,
    val username: String,
    val email: String,
    val profileUrl: String? = null,
    val groupIds: List<String>? = null,
    val pastGroupIds: List<String>? = null,
    val createdAt: Long
)

fun UserDetailsResponse.toEntity(): UserEntity {
    return UserEntity(
        userId = uid,
        username = username,
        email = email,
        profileUrl = profileUrl,
        groupIds = groupIds,
        pastGroupIds = pastGroupIds,
        createdAt = createdAt
    )
}
