package com.example.housify.domain.model

import com.example.housify.data.local.entity.GroupEntity

enum class UserRole {
    ADMIN,
    MEMBER,
    UNKNOWN
}

// domain model for ui
data class Group(
    val id: String,
    val name: String,
    val numberOfAssignedTasks: Int,
    val admin: User,
    val members: List<User>,
    val createdAt: Long,
    val invitationCode: String,
    val isUserAdmin: Boolean
)

data class GroupEntry(
    val id: String,
    val name: String,
    val admin: User,
    val members: List<User>,
    val latestLeaderboardEntries: List<LeaderboardEntry>,
    val tasks: List<Task>,
    val isUserAdmin: Boolean,
    val invitationCode: String
)

fun Group.toEntity(userId: String): GroupEntity {
    return GroupEntity(
        userId = userId,
        groupId = id,
        name = name,
        numberOfAssignedTasks = numberOfAssignedTasks,
        admin = admin,
        members = members,
        createdAt = createdAt,
        invitationCode = invitationCode,
        isUserAdmin = isUserAdmin
    )
}


