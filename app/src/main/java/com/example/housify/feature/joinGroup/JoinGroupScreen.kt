package com.example.housify.feature.joinGroup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.R
import com.example.housify.ui.components.ErrorFullScreen
import com.example.housify.ui.components.LoadingFullScreen
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun JoinGroupScreen(
    onShowSnackbar: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: JoinGroupViewModel = hiltViewModel(),
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
            LaunchedEffect(Unit) {
                viewModel.successMessage.collect { message ->
                    onShowSnackbar(message)
                }
            }

            LaunchedEffect(Unit) {
                viewModel.onBackEvent.collect {
                    onBack()
                }
            }

            JoinGroupContent(
                uiState = uiState,
                onBack = onBack,
                onJoinGroup = { viewModel.joinGroup() },
            )
        }
    }
}

@Composable
fun JoinGroupContent(
    uiState: JoinGroupUiState,
    onBack: () -> Unit,
    onJoinGroup: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_with_app_name_only),
                    contentDescription = "Logo",
                    modifier = Modifier.size(150.dp)
                )

                Text(
                    text = buildAnnotatedString {
                        append("Are you sure you want to join the group '")
                        withStyle(
                            style = MaterialTheme.typography.bodyLarge.toSpanStyle()
                                .copy(fontWeight = FontWeight.Bold)
                        ) {
                            append(uiState.groupName)
                        }
                        append("'")
                    },
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onJoinGroup,
                        enabled = !uiState.isJoinGroupLoading,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        shape = RoundedCornerShape(30),
                    ) {
                        if (uiState.isJoinGroupLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Join")
                        }
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        shape = RoundedCornerShape(30),
                    ) {
                        Text(text = "Cancel")
                    }
                }

                if (uiState.joinGroupError != null) {
                    Text(
                        text = uiState.joinGroupError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinGroupScreenPreview() {
    HousifyTheme {
        JoinGroupContent(
            uiState = JoinGroupUiState(
                groupName = "Group Name",
                isLoading = false,
                error = null
            ),
            onBack = {},
            onJoinGroup = {}
        )
    }
}