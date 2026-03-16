package com.example.housify.feature.auth

import com.example.housify.data.remote.dto.RegisterResponse
import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val user: FirebaseUser) : AuthState()
    data class RegisterSuccess(val response: RegisterResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}