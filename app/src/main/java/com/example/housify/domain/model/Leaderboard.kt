package com.example.housify.domain.model

data class Leaderboard(
    val id: String,
    val groupId: String,
    val startDate: String,
    val endDate: String,
    var entries: List<LeaderboardEntry>,
    val week: Int
)

data class LeaderboardEntry(val user: User, val rating: Float)

data class LeaderboardHistory(
    val id: String,
    val groupId: String,
    val startDate: String,
    val endDate: String,
    val week: Int
)
