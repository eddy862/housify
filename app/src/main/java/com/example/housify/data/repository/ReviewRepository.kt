package com.example.housify.data.repository

import android.util.Log
import com.example.housify.Resource
import com.example.housify.data.local.dao.RatingDao
import com.example.housify.data.local.datastore.UserPreferencesDataStore
import com.example.housify.data.local.entity.toDomain
import com.example.housify.data.remote.ReviewApiService
import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.remote.dto.PeerReview
import com.example.housify.data.remote.dto.PeerReviewRequest
import com.example.housify.data.remote.dto.RatingRequest
import com.example.housify.data.remote.dto.SignedUrlResponse
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.data.remote.dto.toEntity
import com.example.housify.todayLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val reviewApiService: ReviewApiService,
    private val userPreferences: UserPreferencesDataStore,
    private val ratingDao: RatingDao,
) {
    suspend fun createPeerReview(
        groupId: String,
        taskId: String,
        taskInstanceId: String,
        media: List<MediaItem>,
        description: String
    ) {
        reviewApiService.createPeerReview(
            request = PeerReviewRequest(
                groupId, taskId, taskInstanceId, media, description
            )
        )
    }

    // Upload to Google Cloud Storage
    suspend fun uploadToGCS(
        signedUrl: String,
        bytes: ByteArray,
        mimeType: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val okHttpClient = OkHttpClient()

            val requestBody = bytes.toRequestBody(mimeType.toMediaType())

            val request = Request.Builder()
                .url(signedUrl)
                .put(requestBody)
                .addHeader("Content-Type", mimeType)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("UploadToGCS", "Upload successful. Code: ${response.code}")
                true
            } else {
                Log.e(
                    "UploadToGCS",
                    "Upload failed. Code: ${response.code}, Message: ${response.message}"
                )
                false
            }
        } catch (e: Exception) {
            Log.e("UploadToGCS", "Exception during upload: ${e.localizedMessage}", e)
            false
        }
    }

    suspend fun getUploadSignedUrl(
        groupId: String,
        taskId: String,
        taskInstanceId: String,
        mimeType: String
    ): SignedUrlResponse {
        return reviewApiService.getUploadSignedUrl(
            groupId = groupId,
            taskId = taskId,
            taskInstanceId = taskInstanceId,
            mimeType = mimeType
        )
    }

    suspend fun createRating(
        groupId: String,
        reviewId: String,
        cleanlinessScore: Int,
        punctualityScore: Int,
        comment: String
    ) {
        reviewApiService.createRating(
            groupId = groupId,
            reviewId = reviewId,
            request = RatingRequest(
                cleanlinessScore = cleanlinessScore,
                punctualityScore = punctualityScore,
                comment = comment
            )
        )
    }

    fun getUncompletedTask(
    ): Flow<Resource<List<UncompletedRating>>> = flow {
        emit(Resource.Loading())

        val cachedRatings = try {
            val currentUserId =
                userPreferences.getUserId.first() ?: throw Exception("User is not logged in.")
            ratingDao.getRatingsByUserId(currentUserId).first().map { it.toDomain() }
        } catch (e: Exception) {
            Log.d("ReviewRepository", "Exception: ${e.message}")
            emit(Resource.Error("Could not load user data: ${e.message}"))
            return@flow
        }

        emit(Resource.Success(cachedRatings))

        try {
            val currentUserId =
                userPreferences.getUserId.first()!!

            val uncompletedTasks = reviewApiService.getUncompletedTask().body()
                ?: throw Exception("Failed to fetch uncompleted tasks")

            Log.d("ReviewRepository", "Ratings: $uncompletedTasks")

            ratingDao.deleteAllByUserId(currentUserId)
            ratingDao.insertAll(uncompletedTasks.map { it.toEntity(currentUserId) })

            val newCachedRatings =
                ratingDao.getRatingsByUserId(currentUserId)

            emit(Resource.Success(newCachedRatings.first().map { it.toDomain() }))
        } catch (e: IOException) {
            Log.d("ReviewRepository", "IOException: ${e.message}")
            emit(
                Resource.Error(
                    "Couldn't connect to the server. Please check your internet connection.",
                    data = cachedRatings
                )
            )
        } catch (e: HttpException) {
            Log.d("ReviewRepository", "HttpException: ${e.message}")
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage, data = cachedRatings))
        } catch (e: Exception) {
            Log.d("ReviewRepository", "Exception: ${e.message}")
            emit(
                Resource.Error(
                    "An unexpected error occurred: ${e.message}",
                    data = cachedRatings
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getPeerReviewByReviewId(
        reviewId: String
    ): PeerReview {
        return reviewApiService.getPeerReviewByReviewId(reviewId)
    }

    suspend fun getDownloadSignedUrl(
        images: List<MediaItem>
    ): List<DownloadSignedUrl> {
        return reviewApiService.getDownloadSignedUrl(images)
    }
}