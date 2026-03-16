package com.example.housify.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.housify.Resource
import com.example.housify.convertMillisToLocalDate
import com.example.housify.data.local.dao.TaskDao
import com.example.housify.data.local.datastore.UserPreferencesDataStore
import com.example.housify.data.local.entity.toDomain
import com.example.housify.data.remote.GroupApiService
import com.example.housify.data.remote.TaskApiService
import com.example.housify.data.remote.dto.CompletedTask
import com.example.housify.data.remote.dto.CreateTaskReq
import com.example.housify.data.remote.dto.FutureTask
import com.example.housify.data.remote.dto.TaskScheduleReq
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.data.remote.dto.toDomain
import com.example.housify.data.remote.dto.toEntity
import com.example.housify.di.RetrofitProvider
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.TaskDetails
import com.example.housify.domain.model.UserRole
import com.example.housify.domain.model.toDto
import com.example.housify.domain.repository_interfaces.TaskRepository
import com.example.housify.todayLocalDate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val provider: RetrofitProvider,
    private val firebaseAuth: FirebaseAuth,
    private val taskDao: TaskDao,
    private val userPreferences: UserPreferencesDataStore
) : TaskRepository {
    private suspend fun groupApi(): GroupApiService {
        return provider.get().create(GroupApiService::class.java)
    }

    private suspend fun taskApi(): TaskApiService {
        return provider.get().create(TaskApiService::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createTask(
        groupId: String,
        title: String,
        place: String,
        assigneeIds: List<String>,
        repeat: Repeat,
        startDate: Long,
        isRotational: Boolean
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = taskApi().createTask(
                groupId,
                CreateTaskReq(
                    title,
                    place,
                    assigneeIds,
                    TaskScheduleReq(
                        repeat.toDto(),
                        convertMillisToLocalDate(startDate),
                        isRotational
                    )
                )
            )

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

    override fun getTaskDetails(
        taskId: String,
        groupId: String
    ): Flow<Resource<TaskDetails>> = flow {
        emit(Resource.Loading())

        try {
            val currentUserId =
                firebaseAuth.currentUser?.uid ?: throw Exception("User is not logged in.")


            val domainTaskDetails = coroutineScope {
                val allMembersDeferred = async { groupApi().getGroupMembers(groupId) }
                val allTasksDeferred = async { taskApi().getAllGroupTasks(groupId) }

                val allMembersDto = allMembersDeferred.await()
                val allTasksDto = allTasksDeferred.await()

                val allMembers =
                    allMembersDto.body()?.map { it.toDomain(currentUserId) }
                        ?: throw Exception("Failed to fetch members")
                val admin = allMembers.firstOrNull { it.second == UserRole.ADMIN }?.first
                    ?: throw Exception("Admin not found")

                val allTasks = allTasksDto.body()?.filter { !it.isDeleted }
                    ?.map { it -> it.toDomain(allMembers.map { it.first }, currentUserId) }
                    ?: throw Exception("Failed to fetch tasks")
                val task =
                    allTasks.firstOrNull { it.id == taskId } ?: throw Exception("Task not found")

                val recentSchedule =
                    taskApi().getRecentTaskInstances(groupId, taskId).body()?.map { it ->
                        it.toDomain(allMembers.map { it.first })
                    } ?: throw Exception("Failed to fetch recent schedule")

                TaskDetails(
                    task = task,
                    isUserAdmin = admin.id == currentUserId,
                    recentSchedule = recentSchedule
                )
            }

            emit(Resource.Success(domainTaskDetails))
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

    override fun deleteTask(
        taskId: String,
        groupId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val res = taskApi().deleteTask(groupId, taskId)

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

    fun getTodayTasks(
    ): Flow<Resource<List<TodayTask>>> = flow {
        emit(Resource.Loading())

        val cachedTodayTasks = try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")
            taskDao.getTodayTaskByUserId(currentUserId, todayLocalDate()).first()
                .map { it.toDomain() }
        } catch (e: Exception) {
            Log.d("TaskRepositoryImpl", "Exception: ${e.message}")
            emit(Resource.Error("Could not load user data: ${e.message}"))
            return@flow
        }

        emit(Resource.Success(cachedTodayTasks))

        try {
            val currentUserId =
                userPreferences.getUserId.first()!!

            val todayTasks = taskApi().getTodayTasks().body()
                ?: throw Exception("Failed to fetch today tasks")

            Log.d("TaskRepositoryImpl", "Today tasks: $todayTasks")

            taskDao.deleteAllByUserId(currentUserId)
            taskDao.insertAll(todayTasks.map { it.toEntity(currentUserId, todayLocalDate()) })

            val newCachedTodayTasks =
                taskDao.getTodayTaskByUserId(currentUserId, todayLocalDate())

            emit(Resource.Success(newCachedTodayTasks.first().map { it.toDomain() }))
        } catch (e: IOException) {
            Log.d("TaskRepositoryImpl", "IOException: ${e.message}")
            emit(
                Resource.Error(
                    "Couldn't connect to the server. Please check your internet connection.",
                    data = cachedTodayTasks
                )
            )
        } catch (e: HttpException) {
            Log.d("TaskRepositoryImpl", "HttpException: ${e.message}")
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage, data = cachedTodayTasks))
        } catch (e: Exception) {
            Log.d("TaskRepositoryImpl", "Exception: ${e.message}")
            emit(
                Resource.Error(
                    "An unexpected error occurred: ${e.message}",
                    data = cachedTodayTasks
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getFutureTasks(
        date: String
    ): List<FutureTask> {
        return taskApi().getFutureTasks(date)
    }

    suspend fun getCompletedTaskHistory(): List<CompletedTask> {
        return taskApi().getCompletedTaskHistory()
    }
}