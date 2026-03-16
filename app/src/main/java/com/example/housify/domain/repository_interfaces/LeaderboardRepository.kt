package com.example.housify.domain.repository_interfaces

import com.example.housify.Resource
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.model.LeaderboardHistory
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getAllLeaderboards(groupId: String): Flow<Resource<List<LeaderboardHistory>>>

    fun getLeaderboardByWeek(groupId: String, week: Long): Flow<Resource<Leaderboard>>
}
