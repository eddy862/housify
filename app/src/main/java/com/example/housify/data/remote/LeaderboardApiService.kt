package com.example.housify.data.remote

import com.example.housify.data.remote.dto.LeaderboardDto
import com.example.housify.data.remote.dto.LeaderboardRankingDto
import com.example.housify.di.Authenticated
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LeaderboardApiService {
    @Authenticated
    @GET("leaderboard/{groupId}")
    suspend fun getAllLeaderboard(@Path("groupId") groupId: String): Response<List<LeaderboardDto>>

    @Authenticated
    @GET("leaderboard/ranking/{groupId}/{week}")
    suspend fun getLeaderboardRanking(
        @Path("groupId") groupId: String,
        @Path("week") week: Long
    ): Response<List<LeaderboardRankingDto>>
}