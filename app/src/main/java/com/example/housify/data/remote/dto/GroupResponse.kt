package com.example.housify.data.remote.dto

import com.example.housify.domain.model.Group
import com.example.housify.domain.model.User
import com.example.housify.domain.model.UserRole
import com.squareup.moshi.JsonClass

// dto from api
@JsonClass(generateAdapter = true)
data class GroupDto(
    val groupId: String,
    val groupName: String,
    val invitationCode: String,
    val createdBy: String,
    val isDeleted: Boolean,
    val createdAt: Long,
)

@JsonClass(generateAdapter = true)
data class GroupMemberDto(
    val groupId: String,
    val userId: String,
    val username: String,
    val role: String
)

fun GroupMemberDto.toDomain(currentUserId: String): Pair<User, UserRole> {
    return Pair(
        User(
            id = userId,
            name = username,
            isCurrentUser = currentUserId == userId
        ),
        when (this.role) {
            "admin" -> UserRole.ADMIN
            "user" -> UserRole.MEMBER
            else -> UserRole.UNKNOWN
        }
    )
}

fun GroupDto.toDomain(
    members: List<Pair<User, UserRole>>,
    numberOfAssignedTasks: Int,
    currentUserId: String
): Group {
    val admin = members.firstOrNull { it.second == UserRole.ADMIN }?.first
        ?: throw Exception("Admin not found")
    val members = members.filter { it.second == UserRole.MEMBER }.map { it.first }

    return Group(
        id = groupId,
        name = groupName,
        numberOfAssignedTasks = numberOfAssignedTasks,
        admin = admin,
        members = members,
        createdAt = createdAt,
        invitationCode = invitationCode,
        isUserAdmin = admin.id == currentUserId
    )
}