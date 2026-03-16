package com.example.housify.feature.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.housify.data.remote.dto.CompletedTask
import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.feature.tasks.RatingToDoViewModel
import com.example.housify.ui.components.UserAvatar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CompletedTasksScreen(
    completedTasksViewModel: CompletedTasksViewModel = hiltViewModel(),
    ratingTodoViewModel: RatingToDoViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val completedTasks by completedTasksViewModel.completedTasks.collectAsState()
    val downloadSignedUrl by completedTasksViewModel.downloadSignedUrl.collectAsState()
    val isLoading by completedTasksViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        completedTasksViewModel.getCompletedTaskHistory()
    }

    LaunchedEffect(completedTasks) {
        if (completedTasks.isNotEmpty()) {
            val mediaItems = completedTasks.flatMap { it.media }
            if (mediaItems.isNotEmpty()) {
                completedTasksViewModel.getDownloadSignedUrlByMedia(mediaItems)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .shadow(
//                    elevation = 6.dp,
//                    shape = RoundedCornerShape(12.dp)
//                )
//                .background(Color.White)
//                .border(
//                    width = 1.dp,
//                    color = Color.Transparent,
//                    shape = RoundedCornerShape(8.dp)
//                )
//                .padding(horizontal = 24.dp, vertical = 30.dp)
//        ) {
//            Column() {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    // Date
//                    Text(
//                        "10 Oct 2025 3:00pm", style = TextStyle(
//                            fontSize = 14.sp,
//                            color = MaterialTheme.colorScheme.onBackground
//                        )
//                    )
//                    // Assignee
//                    Text(
//                        "2 persons rated this", style = TextStyle(
//                            fontSize = 14.sp,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Text(
//                    text = "Clean the basin",
//                    style = TextStyle(
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.W500,
//                        color = Color.Black
//                    )
//                )
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        "Group 1",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Text(
//                        "\u2022",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//
//                    Spacer(
//                        modifier = Modifier.width(8.dp),
//                    )
//
//                    Icon(
//                        imageVector = Icons.Default.Place,
//                        contentDescription = "Location Icon",
//                        tint = MaterialTheme.colorScheme.onBackground
//                    )
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Text(
//                        text = "Kitchen",
//                        color = MaterialTheme.colorScheme.onBackground,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                HorizontalDivider(
//                    color = MaterialTheme.colorScheme.onBackground,
//                    thickness = 1.dp,
//                    modifier = Modifier.padding(vertical = 10.dp)
//                )
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Text("I am descdription", style = TextStyle(fontSize = 12.sp))
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                ImageGallery(
//                    downloadSignedUrl ?: emptyList()
//                )
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                Comment("Eddy", 5, "Excellent!", "15 Nov 2025", Modifier)
//                HorizontalDivider(
//                    color = MaterialTheme.colorScheme.onBackground,
//                    thickness = 1.dp,
//                    modifier = Modifier.padding(vertical = 10.dp)
//                )
//                Comment("Jayden", 3, "Good", "16 Nov 2025", Modifier)
//            }
//        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (completedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text("No completed tasks")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(completedTasks) { task ->
                    CompletedTaskCard(
                        task = task,
                        mediaItems = downloadSignedUrl?.filter { media -> task.media.any{item -> media.objectPath == item.fileUrl} }
                            ?: emptyList()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CompletedTaskCardPreview() {
    CompletedTaskCard(
        task = CompletedTask(
            title = "Clean the basin",
            groupName = "Group 1",
            place = "Kitchen",
            description = "I am description",
            createdAt = 1234567890,
            media = emptyList(),
            ratings = emptyList()
        ),
        mediaItems = emptyList()
    )
}

@Composable
fun CompletedTaskCard(
    task: CompletedTask,
    mediaItems: List<DownloadSignedUrl>
) {
    val title = task.title
    val groupName = task.groupName
    val place = task.place
    val description = task.description
    val createdAt = task.createdAt
    val personsRatedText = "${task.ratings.size} persons rated this"

    val formattedDate = Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy h:mma", Locale.ENGLISH))

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
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date
                Text(
                    formattedDate,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                // Assignee / rating count
                Text(
                    personsRatedText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
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
                    groupName,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "\u2022",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location Icon",
                    tint = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = place,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onBackground,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                description,
                style = TextStyle(fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            ImageGallery(mediaItems)

            Spacer(modifier = Modifier.height(20.dp))

            // Ratings list
            if (task.ratings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                task.ratings.forEachIndexed { index, rating ->
                    // format rating date (adapt to your Rating model)
                    val ratingDate = Instant.ofEpochMilli(rating.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))

                    Comment(
                        name = rating.name,
                        rating = (rating.punctualityScore + rating.cleanlinessScore) / 2,
                        comment = rating.comment,
                        dateTime = ratingDate
                    )

                    // Divider between comments, but not after the last one
                    if (index < task.ratings.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onBackground,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
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
                                // open full-screen dialog
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

    // 🔍 Full-screen image preview dialog
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

@Preview
@Composable
fun CommentPreview() {
    Comment(
        name = "Eddy",
        rating = 5,
        comment = "Excellent!",
        dateTime = "15 Nov 2025",
    )
}


@Composable
fun Comment(
    name: String,
    rating: Int,
    comment: String,
    dateTime: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Profile Picture Placeholder
            UserAvatar(username = name, size = 30.dp)

            Spacer(Modifier.width(8.dp))

            // Right Side Content
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall
                )

                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "rating",
                            tint = if (index < rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Comment text
        Text(
            text = comment,
            style = TextStyle(
                fontSize = 14.sp,
            )
        )

        Spacer(Modifier.height(8.dp))

        // Date-time text
        Text(
            text = dateTime,
            style = TextStyle(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(Modifier.height(8.dp))
    }
}

//@Preview(showBackground = true)
//@Composable
//fun CompletedTasksScreenPreview() {
//    CompletedTasksScreen(
//        { "123" },
//    )
//}