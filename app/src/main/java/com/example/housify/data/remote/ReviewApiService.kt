package com.example.housify.data.remote

import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.remote.dto.PeerReview
import com.example.housify.data.remote.dto.PeerReviewRequest
import com.example.housify.data.remote.dto.RatingRequest
import com.example.housify.data.remote.dto.SignedUrlResponse
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.di.Authenticated
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {
    @Authenticated
    @POST("reviews")
    suspend fun createPeerReview(@Body request: PeerReviewRequest)

    @Authenticated
    @POST("reviews/{groupId}/{reviewId}")
    suspend fun createRating(
        @Path("groupId") groupId: String,
        @Path("reviewId") reviewId: String,
        @Body request: RatingRequest
    )

    @Authenticated
    @GET("reviews/upload/url/{groupId}/{taskId}/{taskInstanceId}")
    suspend fun getUploadSignedUrl(
        @Path("groupId") groupId: String,
        @Path("taskId") taskId: String,
        @Path("taskInstanceId") taskInstanceId: String,
        @Query("mimeType") mimeType: String
    ): SignedUrlResponse

    @Authenticated
    @GET("reviews/uncompleted/me")
    suspend fun getUncompletedTask(): Response<List<UncompletedRating>>

    @Authenticated
    @GET("reviews/{reviewId}")
    suspend fun getPeerReviewByReviewId(
        @Path("reviewId") reviewId: String
    ): PeerReview

    @Authenticated
    @POST("reviews/download/urls")
    suspend fun getDownloadSignedUrl(
        @Body request: List<MediaItem>
    ): List<DownloadSignedUrl>
}