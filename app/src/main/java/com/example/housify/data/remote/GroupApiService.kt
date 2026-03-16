package com.example.housify.data.remote

import com.example.housify.data.remote.dto.GroupDto
import com.example.housify.data.remote.dto.GroupMemberDto
import com.example.housify.di.Authenticated
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupApiService {
    @Authenticated
    @GET("groups/me")
    suspend fun getAllGroups(): Response<List<GroupDto>>

    @Authenticated
    @GET("groups/{groupId}/members")
    suspend fun getGroupMembers(@Path("groupId") groupId: String): Response<List<GroupMemberDto>>

    @Authenticated
    @POST("groups")
    suspend fun createGroup(@Query("groupName") groupName: String): Response<Unit>

    @Authenticated
    @POST("groups/join")
    suspend fun joinGroup(@Query("invitationCode") invitationCode: String): Response<Unit>

    @Authenticated
    @DELETE("groups/{groupId}/members/{userId}")
    suspend fun removeGroupMember(@Path("groupId") groupId: String, @Path("userId") userId: String) : Response<Unit>

    @Authenticated
    @DELETE("groups/{groupId}/members/me")
    suspend fun leaveGroup(@Path("groupId") groupId: String): Response<Unit>

    @Authenticated
    @DELETE("groups/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: String): Response<Unit>

    @Authenticated
    @PUT("groups/{groupId}")
    suspend fun editGroupName(@Path("groupId") groupId: String, @Query("newGroupName") newGroupName: String): Response<Unit>

    @Authenticated
    @GET("groups/{invitationCode}")
    suspend fun getGroupByInvitationCode(@Path("invitationCode") invitationCode: String): Response<GroupDto>
}

