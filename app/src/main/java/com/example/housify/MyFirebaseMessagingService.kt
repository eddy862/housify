package com.example.housify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.housify.data.remote.dto.DeviceToken
import com.example.housify.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationRepository: NotificationRepository

//    Test
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

        val body = DeviceToken(
            token = token,
            deviceModel = deviceModel,
            platform = "android"
        )

        sendTokenToBackend(body)
    }

    private fun sendTokenToBackend(deviceToken: DeviceToken) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationRepository.saveOrUpdateDeviceToken(deviceToken)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Housify"
        val body = message.notification?.body ?: "You have a new message"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "housify_channel"

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Housify Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}