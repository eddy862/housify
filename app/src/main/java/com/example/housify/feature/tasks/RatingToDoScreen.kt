package com.example.housify.feature.tasks

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.UncompletedRating
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RatingToDoScreen(
    viewModel: RatingToDoViewModel = hiltViewModel(),
    rating: UncompletedRating,
    onBack: () -> Unit
) {
    var cleanliness by remember { mutableStateOf(0) }
    var punctuality by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val downloadSignedUrl = viewModel.downloadSignedUrl.collectAsState()

    val ratings = listOf(cleanliness, punctuality).filter { it > 0 }
    val average = if (ratings.isNotEmpty()) ratings.average() else 0.0

    var isSubmitting by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    val hasCleanliness = cleanliness > 0
    val hasPunctuality = punctuality > 0
    val hasComment = comment.isNotBlank()
    val canRate = hasCleanliness && hasPunctuality && hasComment

    val coroutineScope = rememberCoroutineScope()

    val errorMessage: String? = when {
        showValidationError && !hasCleanliness ->
            "Please rate cleanliness."

        showValidationError && !hasPunctuality ->
            "Please rate punctuality."

        showValidationError && !hasComment ->
            "Comment is required."

        else -> null
    }

    LaunchedEffect(Unit) {
        viewModel.getDownloadSignedUrl(
            rating.reviewId
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 30.dp)
            ) {
                Column() {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val formattedDate = Instant.ofEpochMilli(rating.reviewCreatedAt)
                            .atZone(ZoneId.systemDefault())
                            .format(
                                DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy h:mma",
                                    Locale.ENGLISH
                                )
                            )

                        // Date
                        Text(
                            formattedDate, style = TextStyle(
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Assignee
                        Text(
                            "Assigned to ${rating.revieweeName}", style = TextStyle(
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = rating.title,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            rating.groupName,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "\u2022",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp),
                        )

                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location Icon",
                            tint = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = rating.place,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ImageGallery(
                        downloadSignedUrl.value ?: emptyList()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    RatingComponent(
                        title = "Cleanliness",
                        rating = cleanliness,
                        onRatingChanged = { cleanliness = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RatingComponent(
                        title = "Punctuality",
                        rating = punctuality,
                        onRatingChanged = { punctuality = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("Average: $average")
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Text("Comment")

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = false,
                        minLines = 3,
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                )
            }

            Button(
                onClick = {
                    showValidationError = true

                    if (!canRate) {
                        return@Button
                    }

                    showValidationError = false
                    isSubmitting = true

                    coroutineScope.launch {
                        val success = viewModel.createRating(
                            rating.groupId,
                            rating.reviewId,
                            cleanliness,
                            punctuality,
                            comment
                        )

                        isSubmitting = false

                        if (success) {
                            onBack() // only after job finished
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "RATE",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 16.sp
                )
            }
        }

        // Full Screen Loading Overlay
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.1f))
                    // consume all clicks so they don't reach content under
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun RatingComponent(
    title: String,
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    totalStars: Int = 5
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = TextStyle(
                fontSize = 14.sp,
            ),
            modifier = Modifier.width(100.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..totalStars) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $i",
                    tint = if (i <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onRatingChanged(i) }
                )
            }
        }
    }
}

@Composable
fun ImageGallery(
    mediaItems: List<DownloadSignedUrl>
) {
    if (mediaItems.isEmpty()) {
        Text(
            text = "No media available",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(8.dp)
        )
        return
    }

    val context = LocalContext.current
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mediaItems) { media ->
            when {
                media.mimeType.startsWith("image/") -> {
                    AsyncImage(
                        model = media.signedDownloadUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                // 👈 open full-screen dialog
                                selectedImageUrl = media.signedDownloadUrl
                            },
                        contentScale = ContentScale.Crop
                    )
                }

                media.mimeType.startsWith("video/") -> {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(
                                        Uri.parse(media.signedDownloadUrl),
                                        media.mimeType
                                    )
                                }
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play video",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Unsupported media",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Full-screen image preview dialog
    if (selectedImageUrl != null) {
        Dialog(
            onDismissRequest = { selectedImageUrl = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { selectedImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
