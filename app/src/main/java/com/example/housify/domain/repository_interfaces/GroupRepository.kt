package com.example.housify.domain.repository_interfaces

import com.example.housify.Resource
import com.example.housify.domain.model.Group
import com.example.housify.domain.model.GroupEntry
import com.example.housify.domain.model.LeaderboardEntry
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.User
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getGroups(): Flow<Resource<List<Group>>>

    fun createGroup(groupName: String): Flow<Resource<Unit>>

    fun joinGroup(invitationCode: String): Flow<Resource<Unit>>

    fun deleteGroup(groupId: String): Flow<Resource<Unit>>

    fun editGroupName(groupId: String, newGroupName: String): Flow<Resource<Unit>>

    fun removeMember(groupId: String, memberId: String): Flow<Resource<Unit>>

    fun leaveGroup(groupId: String): Flow<Resource<Unit>>

    fun getGroupEntry(groupId: String): Flow<Resource<GroupEntry>>

    fun getGroupAllUsers(groupId: String): Flow<Resource<List<User>>>

    fun getGroupNameByInvitationCode(invitationCode: String): Flow<Resource<String>>
}
