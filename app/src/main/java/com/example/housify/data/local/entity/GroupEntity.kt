package com.example.housify.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.housify.domain.model.Group
import com.example.housify.domain.model.User

@Entity(
    tableName = "groups",
    indices = [Index(value = ["userId"])]
)
data class GroupEntity(
    @PrimaryKey val groupId: String,
    val userId: String,
    val name: String,
    val numberOfAssignedTasks: Int,
    val admin: User,
    val members: List<User>,
    val createdAt: Long,
    val invitationCode: String,
    val isUserAdmin: Boolean
)

fun GroupEntity.toDomain(): Group {
    return Group(
        id = groupId,
        name = name,
        numberOfAssignedTasks = numberOfAssignedTasks,
        admin = admin,
        members = members,
        createdAt = createdAt,
        invitationCode = invitationCode,
        isUserAdmin = isUserAdmin
    )
}

