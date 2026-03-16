package com.example.housify.data.remote

import com.example.housify.data.remote.dto.DeviceToken
import com.example.housify.di.Authenticated
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApiService {
    @Authenticated
    @POST("device/token")
    suspend fun saveOrUpdateDeviceToken(@Body deviceToken: DeviceToken): Response<Unit>
}