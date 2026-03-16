package com.example.housify.feature.groups.leaderboard.history_list

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.R
import com.example.housify.domain.model.LeaderboardHistory
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun LeaderboardHistoryScreen(
    onSelectLeaderboardHistoryEntry: (Int) -> Unit,
    viewModel: LeaderboardHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            LoadingFullScreen()
        }

        uiState.error != null -> {
            ErrorFullScreen(uiState.error, viewModel::refreshData)
        }

        else -> {
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { viewModel.refreshData() },
            ) {
                LazyColumn {
                    if (uiState.leaderboardHistory.isEmpty()) {
                        item {
                            Text(
                                "No leaderboards found",
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        itemsIndexed(uiState.leaderboardHistory) { index, leaderboard ->
                            LeaderboardHistoryEntryRow(
                                leaderboard = leaderboard,
                                onSelectLeaderboardHistoryEntry = {
                                    onSelectLeaderboardHistoryEntry(
                                        leaderboard.week
                                    )
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardHistoryEntryRow(
    leaderboard: LeaderboardHistory,
    onSelectLeaderboardHistoryEntry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                onClick = onSelectLeaderboardHistoryEntry
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "Week ${leaderboard.week}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "${leaderboard.startDate} - ${leaderboard.endDate}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            painter = painterResource(R.drawable.arrow_right),
            contentDescription = "Select",
            modifier = Modifier.size(20.dp)
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun LeaderboardHistoryScreenPreview() {
    HousifyTheme {
        LeaderboardHistoryScreen(
            onSelectLeaderboardHistoryEntry = {},
        )
    }
}

