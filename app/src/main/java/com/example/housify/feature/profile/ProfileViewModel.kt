package com.example.housify.feature.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.data.remote.dto.UserDetailsResponse
import com.example.housify.data.remote.dto.UserStat
import com.example.housify.data.repository.AuthRepository
import com.example.housify.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _user = MutableStateFlow<UserDetailsResponse?>(null)
    val user: StateFlow<UserDetailsResponse?> = _user.asStateFlow()

    private val _userStat = MutableStateFlow<UserStat?>(null)
    val userStat: StateFlow<UserStat?> = _userStat.asStateFlow()

    var usernameError by mutableStateOf<String?>(null)
        private set

    var isUpdatingUsername by mutableStateOf(false)
        private set

    init {
        if (authRepository.isUserLoggedIn()) {
            getUserFromApi()
        }
    }

    private fun getUserFromApi() {
        viewModelScope.launch {
            authRepository.getUserFromApi().collect {
                when (it) {
                    is Resource.Success -> {
                        _user.value = it.data
                    }

                    is Resource.Error -> {
                        _user.value = it.data
                    }

                    is Resource.Loading -> {
                        _user.value = null
                    }
                }
            }
        }
    }

    fun getUserStat() {
        viewModelScope.launch {
            profileRepository.getUserStat().collect {
                when (it) {
                    is Resource.Success -> {
                        _userStat.value = it.data
                    }

                    is Resource.Error -> {
                        _userStat.value = it.data
                    }

                    is Resource.Loading -> {
                        _userStat.value = null
                    }
                }
            }
        }
    }

    fun updateUsername(newUsername: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (newUsername.isBlank()) {
                usernameError = "Username cannot be empty."
                return@launch
            }

            isUpdatingUsername = true
            usernameError = null

            val error = profileRepository.updateUsername(newUsername)

            isUpdatingUsername = false

            if (error != null) {
                usernameError = error
            } else {
                usernameError = null
                onSuccess()
                getUserFromApi()
            }
        }
    }

    fun clearUsernameError() {
        usernameError = null
    }
}