package com.example.housify.data.repository

import android.util.Log
import com.example.housify.Resource
import com.example.housify.data.local.dao.UserStatDao
import com.example.housify.data.local.datastore.UserPreferencesDataStore
import com.example.housify.data.local.entity.toDomain
import com.example.housify.data.remote.UserApiService
import com.example.housify.data.remote.dto.UserStat
import com.example.housify.data.remote.dto.toEntity
import com.example.housify.di.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


class ProfileRepository @Inject constructor(
    private val provider: RetrofitProvider,
    private val userPreferences: UserPreferencesDataStore,
    private val userStatDao: UserStatDao
) {
    private suspend fun userApi(): UserApiService {
        return provider.get().create(UserApiService::class.java)
    }

    fun getUserStat(): Flow<Resource<UserStat>> = flow {
        Log.d("ProfileRepository", "getUserStat() called")
        emit(Resource.Loading())

        val cachedUserStat: UserStat? = try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")
            userStatDao.getUserStat(currentUserId).first()?.toDomain()
        } catch (e: Exception) {
            Log.d("ProfileRepository", "Exception: ${e.message}")
            return@flow
        }

        Log.d("ProfileRepository", "Cached user stat: $cachedUserStat")

        if (cachedUserStat != null) emit(Resource.Success(cachedUserStat))

        try {
            val currentUserId =
                userPreferences.getUserId.first()!!

            val userStat =
                userApi().getUserStat().body() ?: throw Exception("Failed to fetch user stat")

            Log.d("ProfileRepository", "User stat: $userStat")

            userStatDao.insertUserStat(userStat.toEntity(currentUserId))

            val newCachedUserStat =
                userStatDao.getUserStat(currentUserId).first()?.toDomain()!!

            emit(Resource.Success(newCachedUserStat))
        } catch (e: IOException) {
            Log.d("ProfileRepository", "IOException: ${e.message}")
            emit(
                Resource.Error(
                    "Couldn't connect to the server. Please check your internet connection.",
                    data = cachedUserStat
                )
            )
        } catch (e: HttpException) {
            Log.d("ProfileRepository", "HttpException: ${e.message}")
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage, data = cachedUserStat))
        } catch (e: Exception) {
            Log.d("ProfileRepository", "Exception: ${e.message}")
            emit(
                Resource.Error(
                    "An unexpected error occurred: ${e.message}",
                    data = cachedUserStat
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    suspend fun updateUsername(newUsername: String): String? {
        return try {
            val response = userApi().updateUsername(newUsername)

            if (response.isSuccessful) {
                null
            } else {
                // handle HTTP error codes here
                if (response.code() == 409) {
                    "Username already taken."
                } else {
                    "Unknown server error: ${response.code()}"
                }
            }
        } catch (e: HttpException) {
            if (e.code() == 409) {
                "Username already taken."
            } else {
                "Server error: ${e.code()}"
            }
        } catch (e: Exception) {
            "Network error: ${e.message ?: "please try again."}"
        }
    }
}
