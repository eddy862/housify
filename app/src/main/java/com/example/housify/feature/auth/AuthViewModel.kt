package com.example.housify.feature.auth

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.data.remote.dto.DeviceToken
import com.example.housify.data.repository.AuthRepository
import com.example.housify.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Login with Firebase
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = authRepository.signInWithEmail(email, password)

                result.onSuccess { user ->
                    _authState.value = AuthState.LoginSuccess(user)

                    sendDeviceTokenAfterLogin()
                }.onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    // Register new user with backend API
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val response = authRepository.register(
                    email = email,
                    password = password,
                    username = username
                )

                _authState.value = AuthState.RegisterSuccess(response)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun sendDeviceTokenAfterLogin() {
        viewModelScope.launch {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

            val body = DeviceToken(
                token = fcmToken,
                deviceModel = deviceModel,
                platform = "android"
            )

            notificationRepository.saveOrUpdateDeviceToken(body)
        }
    }
}