package com.example.housify.data.remote


import com.example.housify.data.remote.dto.CompletedTask
import com.example.housify.data.remote.dto.CreateTaskReq
import com.example.housify.data.remote.dto.FutureTask
import com.example.housify.data.remote.dto.FutureTaskDto
import com.example.housify.data.remote.dto.TaskDto
import com.example.housify.data.remote.dto.TaskInstanceDto
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.di.Authenticated
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApiService {
    @Authenticated
    @POST("tasks/{groupId}")
    suspend fun createTask(@Path("groupId") groupId: String, @Body req: CreateTaskReq): Response<Unit>

    @Authenticated
    @GET("tasks/group/{groupId}")
    suspend fun getAllGroupTasks(@Path("groupId") groupId: String): Response<List<TaskDto>>

    @Authenticated
    @DELETE("tasks/group/{groupId}/{taskId}")
    suspend fun deleteTask(@Path("groupId") groupId: String, @Path("taskId") taskId: String): Response<Unit>

    @Authenticated
    @GET("tasks/group/{groupId}/{taskId}")
    suspend fun getRecentTaskInstances(@Path("groupId") groupId: String, @Path("taskId") taskId: String): Response<List<TaskInstanceDto>>

    @Authenticated
    @GET("tasks/me?date={date}")
    suspend fun getMyTasks(@Path("date") date: String): List<FutureTaskDto>

    @Authenticated
    @GET("tasks/today")
    suspend fun getTodayTasks(): Response<List<TodayTask>>

    @Authenticated
    @GET("tasks/future")
    suspend fun getFutureTasks(
        @Query("date") date: String
    ): List<FutureTask>

    @Authenticated
    @GET("tasks/complete/me")
    suspend fun getCompletedTaskHistory(): List<CompletedTask>
}