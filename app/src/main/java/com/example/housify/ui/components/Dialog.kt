package com.example.housify.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.housify.R
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun SimpleDialog(
    onDismissRequest: () -> Unit,
    @DrawableRes inputPrefixId: Int,
    inputPrefixContentDesc: String,
    inputPlaceholder: String,
    onConfirm: (String) -> Unit,
    buttonText: String,
    loading: Boolean,
    error: String?,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        SimpleDialogCard(
            onDismissRequest = onDismissRequest,
            inputPrefixId = inputPrefixId,
            inputPrefixContentDesc = inputPrefixContentDesc,
            inputPlaceholder = inputPlaceholder,
            onConfirm = onConfirm,
            buttonText = buttonText,
            loading = loading,
            error = error
        )
    }
}

@Composable
fun DualActionDialog(
    onDismissRequest: () -> Unit,
    @DrawableRes inputPrefixId: Int,
    inputPrefixContentDesc: String,
    inputPlaceholder: String,
    onConfirmTop: (String) -> Unit,
    buttonTextTop: String,
    onConfirmBottom: () -> Unit,
    buttonTextBottom: String,
    loadingTop: Boolean,
    errorTop: String?,
    loadingBottom: Boolean,
    errorBottom: String?,
    initialText: String = ""
) {
    Dialog(onDismissRequest = onDismissRequest) {
        DualActionDialogCard(
            onDismissRequest = onDismissRequest,
            inputPrefixId = inputPrefixId,
            inputPrefixContentDesc = inputPrefixContentDesc,
            inputPlaceholder = inputPlaceholder,
            onConfirmTop = onConfirmTop,
            buttonTextTop = buttonTextTop,
            onConfirmBottom = onConfirmBottom,
            buttonTextBottom = buttonTextBottom,
            loadingTop = loadingTop,
            errorTop = errorTop,
            loadingBottom = loadingBottom,
            errorBottom = errorBottom,
            initialText = initialText
        )
    }
}

@Composable
fun SimpleDialogCard(
    onDismissRequest: () -> Unit,
    @DrawableRes inputPrefixId: Int,
    inputPrefixContentDesc: String,
    inputPlaceholder: String,
    onConfirm: (String) -> Unit,
    buttonText: String,
    loading: Boolean,
    error: String?,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel),
                        contentDescription = "close",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            var text by remember { mutableStateOf("") }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(inputPlaceholder) },
                prefix = {
                    Row() {
                        Icon(
                            painter = painterResource(inputPrefixId),
                            contentDescription = inputPrefixContentDesc,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                },
                shape = RoundedCornerShape(20),
                enabled = !loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onConfirm(text) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 15.dp),
                shape = RoundedCornerShape(30),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text((buttonText).uppercase())
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun DualActionDialogCard(
    onDismissRequest: () -> Unit,
    @DrawableRes inputPrefixId: Int,
    inputPrefixContentDesc: String,
    inputPlaceholder: String,
    onConfirmTop: (String) -> Unit,
    buttonTextTop: String,
    onConfirmBottom: () -> Unit,
    buttonTextBottom: String,
    loadingTop: Boolean,
    errorTop: String?,
    loadingBottom: Boolean,
    errorBottom: String?,
    initialText: String = ""
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel),
                        contentDescription = "close",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            var text by remember { mutableStateOf(initialText) }

            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(inputPlaceholder) },
                    prefix = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(inputPrefixId),
                                contentDescription = inputPrefixContentDesc,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    },
                    shape = RoundedCornerShape(20),
                    enabled = !loadingTop
                )

                Button(
                    onClick = { onConfirmTop(text) },
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    shape = RoundedCornerShape(30),
                    enabled = !loadingTop
                ) {
                    if (loadingTop) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text((buttonTextTop).uppercase())
                    }
                }
            }

            if (errorTop != null) {
                Text(
                    text = errorTop,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onConfirmBottom,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 15.dp),
                shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                enabled = !loadingBottom
            ) {
                if (loadingBottom) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text((buttonTextBottom).uppercase())
                }
            }

            if (errorBottom != null) {
                Text(
                    text = errorBottom,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun SimpleConfirmationDialogCard(
    onDismissRequest: () -> Unit,
    title: String,
    content: String,
    onConfirm: () -> Unit,
    confirmText: String,
    loading: Boolean,
    error: String?,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel),
                        contentDescription = "close",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(20.dp))

            Text(content, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(30),
                ) {
                    Text("CANCEL")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    shape = RoundedCornerShape(30),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text((confirmText).uppercase())
                    }
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun SimpleAlertDialogCard(
    onDismissRequest: () -> Unit,
    title: String,
    content: String,
    onConfirm: () -> Unit,
    confirmText: String,
    loading: Boolean,
    error: String?,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Close (X) icon on top-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.cancel),
                        contentDescription = "close",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Text(title, style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(20.dp))

            Text(content, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(20.dp))

            // Single OK / confirm button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 15.dp),
                shape = RoundedCornerShape(30),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(confirmText.uppercase())
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Preview
@Composable
fun SimpleConfirmationDialogCardPreview() {
    HousifyTheme {
        SimpleConfirmationDialogCard(
            onDismissRequest = {},
            title = "Title",
            content = "Content",
            onConfirm = {},
            confirmText = "Confirm",
            loading = false,
            error = null
        )
    }
}

@Preview
@Composable
fun SimpleDialogCardPreview() {
    HousifyTheme(dynamicColor = false) {
        SimpleDialogCard(
            onDismissRequest = {},
            inputPrefixId = R.drawable.group_name,
            inputPrefixContentDesc = "group name",
            inputPlaceholder = "Group name",
            onConfirm = {},
            buttonText = "Create",
            loading = false,
            error = null
        )
    }
}

@Preview
@Composable
fun DualActionDialogCardPreview() {
    HousifyTheme(dynamicColor = false) {
        DualActionDialogCard(
            onDismissRequest = {},
            inputPrefixId = R.drawable.group_name,
            inputPrefixContentDesc = "group name",
            inputPlaceholder = "Group name",
            onConfirmTop = {},
            buttonTextTop = "Save",
            onConfirmBottom = {},
            buttonTextBottom = "Leave & Delete Group",
            loadingTop = false,
            errorTop = null,
            loadingBottom = false,
            errorBottom = null
        )
    }
}