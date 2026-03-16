package com.example.housify.data.remote

import com.example.housify.data.remote.dto.RegisterRequest
import com.example.housify.data.remote.dto.RegisterResponse
import com.example.housify.data.remote.dto.UserDetailsResponse
import com.example.housify.data.remote.dto.UserStat
import com.example.housify.di.Authenticated
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserApiService {
    @POST("users")
    suspend fun register(@Body req: RegisterRequest): RegisterResponse

    @Authenticated
    @GET("users/me")
    suspend fun getUserFromApi(): Response<UserDetailsResponse>

    @Authenticated
    @GET(" users/me/stat")
    suspend fun getUserStat(): Response<UserStat>

    @Authenticated
    @PUT("users/me/username")
    suspend fun updateUsername(
        @Query("newUsername") newUsername: String
    ): Response<Unit>
}