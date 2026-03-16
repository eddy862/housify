package com.example.housify.data.repository

import com.example.housify.Resource
import com.example.housify.data.local.dao.GroupDao
import com.example.housify.data.local.datastore.UserPreferencesDataStore
import com.example.housify.data.local.entity.toDomain
import com.example.housify.data.remote.GroupApiService
import com.example.housify.data.remote.LeaderboardApiService
import com.example.housify.data.remote.TaskApiService
import com.example.housify.data.remote.dto.toDomain
import com.example.housify.di.RetrofitProvider
import com.example.housify.domain.model.Group
import com.example.housify.domain.model.GroupEntry
import com.example.housify.domain.model.User
import com.example.housify.domain.model.UserRole
import com.example.housify.domain.model.toEntity
import com.example.housify.domain.repository_interfaces.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import kotlin.collections.flatten

class GroupRepositoryImpl @Inject constructor(
    private val provider: RetrofitProvider,
    private val groupDao: GroupDao,
    private val userPreferences: UserPreferencesDataStore
) : GroupRepository {
    private suspend fun groupApi(): GroupApiService {
        return provider.get().create(GroupApiService::class.java)
    }

    private suspend fun leaderboardApi(): LeaderboardApiService {
        return provider.get().create(LeaderboardApiService::class.java)
    }

    private suspend fun taskApi(): TaskApiService {
        return provider.get().create(TaskApiService::class.java)
    }

    override fun getGroups(): Flow<Resource<List<Group>>> = flow {
        emit(Resource.Loading())

        val cachedGroups = try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")
            groupDao.getGroupsByUserId(currentUserId).first().map { it.toDomain() }
        } catch (e: Exception) {
            emit(Resource.Error("Could not load user data: ${e.message}"))
            return@flow
        }

        emit(Resource.Success(cachedGroups))

        try {
            val currentUserId =
                userPreferences.getUserId.first()!!

            val groupsDto = groupApi().getAllGroups()

            val groups = groupsDto.body()?.filter { !it.isDeleted }?.sortedBy { it.createdAt }
                ?: throw Exception("Failed to fetch groups")

            val domainGroups = coroutineScope {
                val membersEachGroupDto = groups.map { group ->
                    async {
                        groupApi().getGroupMembers(group.groupId)
                    }
                }
                val allMembersDto = membersEachGroupDto.awaitAll()
                val allMembers = allMembersDto.map { it ->
                    it.body()?.map { it.toDomain(currentUserId) }
                        ?: throw Exception("Failed to fetch members")
                }
                val allMembersWithoutRole: List<User> = allMembers.flatten().map { it.first }

                val tasksEachGroupDto = groups.map { group ->
                    async {
                        taskApi().getAllGroupTasks(group.groupId)
                    }
                }
                val allTasksDto = tasksEachGroupDto.awaitAll()
                val allTasks = allTasksDto.map { it ->
                    it.body()?.filter { !it.isDeleted }?.map {
                        it.toDomain(allMembersWithoutRole, currentUserId)
                    } ?: throw Exception("Failed to fetch tasks")
                }

                // for each group, how many tasks are assigned to user
                val numOfTasksAssignedToUser = mutableListOf<Int>()
                allTasks.forEachIndexed { index, tasks ->
                    numOfTasksAssignedToUser.add(
                        index,
                        tasks.filter { it.assignees.any { assignee -> assignee.id == currentUserId } }.size
                    )
                }

                groups.mapIndexed { index, groupDto ->
                    val members = allMembers[index]
                    val numOfAssignedTasks = numOfTasksAssignedToUser[index]
                    groupDto.toDomain(members, numOfAssignedTasks, currentUserId)
                }
            }
            // end of network logic

            // if network logic successful, update db
            groupDao.deleteAllByUserId(currentUserId)
            groupDao.insertAll(domainGroups.map { it.toEntity(currentUserId) })

            // emit fresh data from db
            val newCachedGroups =
                groupDao.getGroupsByUserId(currentUserId).first().map { it.toDomain() }
            emit(Resource.Success(newCachedGroups))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage, data = cachedGroups))
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    "You are offline Showing cached data.",
                    data = cachedGroups
                )
            )
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}", data = cachedGroups))
        }
    }.flowOn(Dispatchers.IO) // this is required if using local db

    override fun createGroup(groupName: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = groupApi().createGroup(groupName)

            if (res.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (res.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. Error code: ${res.code()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun joinGroup(invitationCode: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = groupApi().joinGroup(invitationCode)

            if (res.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (res.code()) {
                    409 -> "Invalid invitation code."
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. Error code: ${res.code()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun deleteGroup(groupId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = groupApi().deleteGroup(groupId)

            if (res.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (res.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. Error code: ${res.code()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun editGroupName(
        groupId: String,
        newGroupName: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = groupApi().editGroupName(groupId, newGroupName)

            if (res.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (res.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. Error code: ${res.code()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun removeMember(
        groupId: String,
        memberId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = groupApi().removeGroupMember(groupId, memberId)

            if (res.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (res.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. Error code: ${res.code()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun leaveGroup(groupId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = groupApi().leaveGroup(groupId)

            if (res.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (res.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. Error code: ${res.code()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun getGroupEntry(groupId: String): Flow<Resource<GroupEntry>> = flow {
        emit(Resource.Loading())

        try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")

            val groupEntry = coroutineScope {
                // 1. Start fetching members, tasks, and all leaderboards in parallel
                val membersDeferred = async { groupApi().getGroupMembers(groupId) }
                val tasksDeferred = async { taskApi().getAllGroupTasks(groupId) }
                val allLeaderboardsDeferred =
                    async { leaderboardApi().getAllLeaderboard(groupId) }
                val groupDetailsDeferred = async { groupApi().getAllGroups() }


                // 2. Wait for the parallel calls to complete
                val memberDto = membersDeferred.await()
                val taskDto = tasksDeferred.await()
                val allLeaderboardsDto = allLeaderboardsDeferred.await()
                val groupDetailsDto = groupDetailsDeferred.await()

                // 3. Process the results
                val members = memberDto.body()?.map { it.toDomain(currentUserId) }
                    ?: throw Exception("Failed to fetch members")
                val admin = members.firstOrNull { it.second == UserRole.ADMIN }?.first
                    ?: throw Exception("Admin not found")
                val tasks =
                    taskDto.body()?.filter { !it.isDeleted }?.sortedBy { it.createdAt }
                        ?.map { it -> it.toDomain(members.map { it.first }, currentUserId) }
                        ?: throw Exception("Failed to fetch tasks")
                val allLeaderboards = allLeaderboardsDto.body()
                    ?: throw Exception("Failed to fetch leaderboards")
                val groupDetails = groupDetailsDto.body()?.filter { !it.isDeleted }
                    ?.firstOrNull { it.groupId == groupId }
                    ?: throw Exception("Failed to fetch group details")

                // 4. Perform the final dependent network call
                val latestLeaderboardDto = allLeaderboards.maxByOrNull { it.week }
                    ?: throw Exception("No leaderboards found")
                val latestLeaderboardEntriesDto =
                    leaderboardApi().getLeaderboardRanking(
                        groupId,
                        latestLeaderboardDto.week
                    )
                val latestLeaderboardEntries =
                    latestLeaderboardEntriesDto.body()?.map { it.toDomain() }
                        ?: throw Exception("Failed to fetch latest leaderboard entries")

                // 5. Construct the final object
                GroupEntry(
                    id = groupId,
                    name = groupDetails.groupName,
                    admin = admin,
                    members = members.map { it.first }.filter { it.id != admin.id },
                    latestLeaderboardEntries = latestLeaderboardEntries,
                    tasks = tasks,
                    isUserAdmin = admin.id == currentUserId,
                    invitationCode = groupDetails.invitationCode
                )
            }

            emit(Resource.Success(groupEntry))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun getGroupAllUsers(groupId: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())

        try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")

            val usersDto = groupApi().getGroupMembers(groupId)

            val users = usersDto.body()?.map { it.toDomain(currentUserId) }?.map { it.first }
                ?: throw Exception("Failed to fetch members")

            emit(Resource.Success(users))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    override fun getGroupNameByInvitationCode(invitationCode: String): Flow<Resource<String>> =
        flow {
            emit(Resource.Loading())

            try {
                val groupDto = groupApi().getGroupByInvitationCode(invitationCode)

                val groupName =
                    groupDto.body()?.groupName ?: throw Exception("Failed to fetch group name")

                emit(Resource.Success(groupName))
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
                }
                emit(Resource.Error(errorMessage))
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
            } catch (e: Exception) {
                emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            }
        }
}