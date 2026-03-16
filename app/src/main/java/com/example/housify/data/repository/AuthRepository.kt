package com.example.housify.data.repository

import android.util.Log
import com.example.housify.Resource
import com.example.housify.data.local.dao.UserDao
import com.example.housify.data.local.datastore.UserPreferencesDataStore
import com.example.housify.data.local.entity.toDomain
import com.example.housify.data.remote.UserApiService
import com.example.housify.data.remote.dto.RegisterRequest
import com.example.housify.data.remote.dto.RegisterResponse
import com.example.housify.data.remote.dto.UserDetailsResponse
import com.example.housify.data.remote.dto.toEntity
import com.example.housify.di.RetrofitProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val provider: RetrofitProvider,
    private val userPreferences: UserPreferencesDataStore,
    private val userDao: UserDao
) {
    private suspend fun userApi(): UserApiService {
        return provider.get().create(UserApiService::class.java)
    }

    fun changeUsername(newUsername: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Log.e("FirebaseAuth", "No logged-in user")
            return
        }

        val profileUpdates = userProfileChangeRequest {
            displayName = newUsername
        }

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Username changed to: $newUsername")
                } else {
                    Log.e("FirebaseAuth", "Failed to update username", task.exception)
                }
            }
    }

    // Get current user
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun getUserFromApi(): Flow<Resource<UserDetailsResponse>> = flow {
        emit(Resource.Loading())

        val cachedUserDetails: UserDetailsResponse? = try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")
            val userEntity = userDao.getUserById(currentUserId).first()
            userEntity?.toDomain()
        } catch (e: Exception) {
            Log.d("AuthRepository", "Exception: ${e.message}")
            return@flow
        }

        if (cachedUserDetails != null) emit(Resource.Success(cachedUserDetails))

        try {
            val currentUserId =
                userPreferences.getUserId.first()!!

            val user =
                userApi().getUserFromApi().body() ?: throw Exception("Failed to fetch user")

            Log.d("AuthRepository", "User: $user")

            userDao.upsertUser(user.toEntity())

            val newCachedUserDetails =
                userDao.getUserById(currentUserId).first()?.toDomain()!!

            emit(Resource.Success(newCachedUserDetails))
        } catch (e: IOException) {
            Log.d("AuthRepository", "IOException: ${e.message}")
            emit(
                Resource.Error(
                    "Couldn't connect to the server. Please check your internet connection.",
                    data = cachedUserDetails
                )
            )
        } catch (e: HttpException) {
            Log.d("AuthRepository", "HttpException: ${e.message}")
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage, data = cachedUserDetails))
        } catch (e: Exception) {
            Log.d("AuthRepository", "Exception: ${e.message}")
            emit(
                Resource.Error(
                    "An unexpected error occurred: ${e.message}",
                    data = cachedUserDetails
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    // Sign in with email and password
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user!!
            userPreferences.saveUserId(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign out
    suspend fun signOut() {
        firebaseAuth.signOut()
        userPreferences.clearUserId()
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    suspend fun register(email: String, password: String, username: String): RegisterResponse {
        return try {
            userApi().register(
                req = RegisterRequest(
                    username = username,
                    email = email,
                    password = password
                )
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = errorBody?.let {
                try {
                    val json = JSONObject(it)
                    json.optString("message", "Registration failed")
                } catch (_: Exception) {
                    "Registration failed"
                }
            } ?: "Registration failed"

            throw Exception(errorMessage)
        }
    }
}