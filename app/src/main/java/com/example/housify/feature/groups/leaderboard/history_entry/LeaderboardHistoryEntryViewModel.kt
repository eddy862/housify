package com.example.housify.feature.groups.leaderboard.history_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.domain.model.Leaderboard
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardHistoryEntryUiState(
    val leaderboard: Leaderboard? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LeaderboardHistoryEntryViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val week: Int = checkNotNull(savedStateHandle["week"])

    private val _uiState = MutableStateFlow(LeaderboardHistoryEntryUiState())
    val uiState: StateFlow<LeaderboardHistoryEntryUiState> = _uiState.asStateFlow()

    init {
        fetchLatestData()
    }

    fun refreshData() {
        fetchLatestData()
    }

    private fun fetchLatestData() {
        viewModelScope.launch {
            leaderboardRepository.getLeaderboardByWeek(groupId, week.toLong())
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    error = null,
                                    isLoading = false,
                                    leaderboard = resource.data
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    error = resource.message
                                        ?: "An unknown error occurred",
                                    isLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    error = null,
                                    isLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }
}