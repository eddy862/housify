package com.example.housify.feature.hostIp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun UpdateHostIpScreen(
    onShowSnackbar: (String) -> Unit,
    viewModel: UpdateHostIpViewModel = hiltViewModel()
) {
    val currentHostIp by viewModel.hostIp.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.successMessage.collect { message ->
            onShowSnackbar(message)
        }
    }

    UpdateHostIpCard(
        currentHostIp = currentHostIp,
        onHostIpChange = { newIp -> viewModel.updateHostIp(newIp) },
        error = error
    )
}

@Composable
fun UpdateHostIpCard(
    currentHostIp: String,
    onHostIpChange: (String) -> Unit,
    error: String? = null
) {
    var newIp by remember(currentHostIp) { mutableStateOf(currentHostIp) }

    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.padding(15.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text("Network Configuration", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                OutlinedTextField(
                    value = newIp,
                    onValueChange = { newIp = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Server Host IP") },
                    shape = RoundedCornerShape(20),
                )

                Button(
                    onClick = { onHostIpChange(newIp) },
                    modifier = Modifier.width(120.dp),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    shape = RoundedCornerShape(30),
                ) {
                    Text("Update IP")
                }
            }

            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Preview
@Composable
fun UpdateHostIpScreenPreview() {
    HousifyTheme {
        UpdateHostIpCard(
            currentHostIp = "192.168.1.1",
            onHostIpChange = {},
        )
    }
}