package com.example.housify.data.remote.dto

import android.annotation.SuppressLint
import com.example.housify.convertMillisToDate
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.model.LeaderboardEntry
import com.example.housify.domain.model.User
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeaderboardDto(
    val groupId: String,
    val boardId: String,
    val week: Long,
    val startDate: Long,
    val endDate: Long
)

@JsonClass(generateAdapter = true)
data class LeaderboardRankingDto(
    val userId: String,
    val username: String,
    val averageScore: Double
)

@SuppressLint("DefaultLocale")
fun LeaderboardRankingDto.toDomain(): LeaderboardEntry {
    val formattedRating = String.format("%.1f", averageScore)

    return LeaderboardEntry(
        user = User(userId, username),
        rating = formattedRating.toFloat()
    )
}

fun LeaderboardDto.toDomain(entries: List<LeaderboardEntry>): Leaderboard {
    return Leaderboard(
        id = boardId,
        groupId = groupId,
        startDate = convertMillisToDate(startDate, true),
        endDate = convertMillisToDate(endDate, true),
        entries = entries,
        week = week.toInt()
    )
}