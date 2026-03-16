package com.example.housify.data.repository

import com.example.housify.data.remote.NotificationApiService
import com.example.housify.data.remote.dto.DeviceToken
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService
) {
    suspend fun saveOrUpdateDeviceToken(deviceToken: DeviceToken) {
        notificationApiService.saveOrUpdateDeviceToken(deviceToken)
    }

//    suspend fun deleteDeviceToken(token: String) {
//        api.deleteToken(token)
//    }
}