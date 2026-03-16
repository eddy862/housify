package com.example.housify.feature.groups.leaderboard.history_entry

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.feature.groups.group_entry.LeaderboardEntry
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun LeaderboardHistoryEntryScreen(
    viewModel: LeaderboardHistoryEntryViewModel = hiltViewModel()
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
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Week ${uiState.leaderboard!!.week}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "(${uiState.leaderboard!!.startDate} - ${uiState.leaderboard!!.endDate})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "Average Rating",
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        uiState.leaderboard!!.entries.forEachIndexed { index, entry ->
                            LeaderboardEntry(
                                index = index,
                                entry = entry
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun LeaderboardHistoryEntryScreenPreview() {
    HousifyTheme {
        LeaderboardHistoryEntryScreen()
    }
}
