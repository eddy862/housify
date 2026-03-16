package com.example.housify.data.repository

import com.example.housify.Resource
import com.example.housify.convertMillisToDate
import com.example.housify.data.remote.LeaderboardApiService
import com.example.housify.data.remote.dto.toDomain
import com.example.housify.di.RetrofitProvider
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.model.LeaderboardHistory
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val provider: RetrofitProvider,
) : LeaderboardRepository {
    private suspend fun leaderboardApi(): LeaderboardApiService {
        return provider.get().create(LeaderboardApiService::class.java)
    }

    override fun getAllLeaderboards(groupId: String): Flow<Resource<List<LeaderboardHistory>>> =
        flow {
            emit(Resource.Loading())

            try {
                val leaderboardsDto = leaderboardApi().getAllLeaderboard(groupId)

                val leaderboardsHistory = leaderboardsDto.body()?.map {
                    LeaderboardHistory(
                        id = it.boardId,
                        groupId = it.groupId,
                        startDate = convertMillisToDate(it.startDate, true),
                        endDate = convertMillisToDate(it.endDate, true),
                        week = it.week.toInt()
                    )
                }?.sortedBy { it.week } ?: throw Exception("Failed to fetch leaderboards")

                emit(Resource.Success(leaderboardsHistory))
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Unauthorized. Please log in again."
                    else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
                }
                emit(Resource.Error(errorMessage))
            } catch (e: IOException) {
                emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
            } catch (e: Exception) {
                emit(Resource.Error("An unexpected error occurred: ${e.message}"))
            }
        }

    override fun getLeaderboardByWeek(
        groupId: String,
        week: Long
    ): Flow<Resource<Leaderboard>> = flow {
        emit(Resource.Loading())

        try {
            val leaderboardEntriesDto = leaderboardApi().getLeaderboardRanking(groupId, week)

            val leaderboardEntries = leaderboardEntriesDto.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to fetch leaderboard entries")

            val leaderboardsDto = leaderboardApi().getAllLeaderboard(groupId)
            val leaderboard =
                leaderboardsDto.body()?.firstOrNull { it.week == week }
                    ?.toDomain(leaderboardEntries) ?: throw Exception("Failed to fetch leaderboard")

            val sortedLeaderboard =
                leaderboard.copy(entries = leaderboard.entries.sortedByDescending { it.rating })

            emit(Resource.Success(sortedLeaderboard))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                401 -> "Unauthorized. Please log in again."
                else -> "An unexpected server error occurred. ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(errorMessage))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}