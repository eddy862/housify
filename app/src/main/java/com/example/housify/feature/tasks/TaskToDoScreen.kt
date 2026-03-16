package com.example.housify.feature.tasks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.ByteArrayOutputStream
import com.example.housify.R
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.data.remote.dto.UploadMedia
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskToDoScreen(
    viewModel: TaskToDoViewModel = hiltViewModel(),
    task: TodayTask,
    onBack: () -> Unit
) {
    var mediaToUpload by remember { mutableStateOf<List<UploadMedia>>(emptyList()) }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    val hasMedia = mediaToUpload.isNotEmpty()
    val hasDescription = description.isNotBlank()
    val canSave = hasMedia && hasDescription

    val errorMessage: String? = when {
        showValidationError && !hasMedia ->
            "Please upload at least 1 photo or video."

        showValidationError && !hasDescription ->
            "Description is required."

        else -> null
    }

    val coroutineScope = rememberCoroutineScope()

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
                    .padding(horizontal = 16.dp, vertical = 30.dp)
            ) {
                Column() {
                    Text(
                        text = task.title,
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            task.groupName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "\u2022",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp),
                        )

                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location Icon",
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = task.place,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Text(
                        "Upload at least 1 photo/video",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    UploadPhotoAndVideoSection(
                        onMediaChanged = { mediaToUpload = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Text("Description", color = MaterialTheme.colorScheme.onSurface)

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = false,
                        minLines = 2,
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

                    if (!canSave) {
                        return@Button
                    }

                    showValidationError = false
                    isSubmitting = true

                    coroutineScope.launch {
                        val success = viewModel.createPeerReview(
                            groupId = task.groupId,
                            taskId = task.taskId,
                            taskInstanceId = task.taskInstanceId,
                            media = mediaToUpload,
                            description = description
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
                    "SUBMIT",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
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
fun UploadPhotoAndVideoSection(
    onMediaChanged: (List<UploadMedia>) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    if (!hasCameraPermission) {
        LaunchedEffect(Unit) {
            launcherPermission.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        var capturedBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
        var photoMedia by remember { mutableStateOf<List<UploadMedia>>(emptyList()) }

        var videoUri by remember { mutableStateOf<Uri?>(null) }
        var videoMedia by remember { mutableStateOf<List<UploadMedia>>(emptyList()) }


        val photoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bmp ->
            bmp?.let { bitmap ->
                capturedBitmaps = capturedBitmaps + bitmap

                val bytes = ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    stream.toByteArray()
                }

                photoMedia = photoMedia + UploadMedia(
                    bytes = bytes,
                    mimeType = "image/jpeg"
                )

                onMediaChanged(photoMedia + videoMedia)
            }
        }

        // ------- VIDEO LAUNCHER -------
        val videoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CaptureVideo()
        ) { success ->
            if (success && videoUri != null) {
                val resolver = context.contentResolver
                val bytes = resolver.openInputStream(videoUri!!)!!.use { it.readBytes() }

                // here we assume mp4; adjust if needed
                videoMedia = listOf(
                    UploadMedia(
                        bytes = bytes,
                        mimeType = "video/mp4"
                    )
                )

                onMediaChanged(photoMedia + videoMedia)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Show all captured photos with fixed weight
                if (capturedBitmaps.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(capturedBitmaps.size) { index ->
                            Box(
                                modifier = Modifier.size(100.dp)
                            ) {
                                Image(
                                    bitmap = capturedBitmaps[index].asImageBitmap(),
                                    contentDescription = "Captured Photo ${index + 1}",
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                // Delete button
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .clickable {
                                            capturedBitmaps =
                                                capturedBitmaps.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            photoMedia = photoMedia.toMutableList().apply {
                                                removeAt(index)
                                            }
                                            onMediaChanged(photoMedia + videoMedia)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_delete),
                                        contentDescription = "Delete Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Camera button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val dashWidth = 5.dp.toPx()
                            val dashGap = 5.dp.toPx()
                            drawRoundRect(
                                color = Color.Gray,
                                size = size,
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(
                                            dashWidth,
                                            dashGap
                                        )
                                    )
                                )
                            )
                        }
                        .clickable {
                            photoLauncher.launch()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.camera),
                            contentDescription = "Take Photo",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            "Take Photo",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Video Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Show captured video thumbnail
                if (videoUri != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {

                        // Thumbnail + Play button
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(videoUri, "video/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_media_play),
                                contentDescription = "Play Video",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Delete button
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .clickable {
                                    videoUri = null
                                    videoMedia = emptyList()
                                    onMediaChanged(photoMedia + videoMedia)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_delete),
                                contentDescription = "Delete Video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                // Video button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val dashWidth = 5.dp.toPx()
                            val dashGap = 5.dp.toPx()
                            drawRoundRect(
                                color = Color.Gray,
                                size = size,
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(
                                            dashWidth,
                                            dashGap
                                        )
                                    )
                                )
                            )
                        }
                        .clickable {
                            val videoFile = File(
                                context.cacheDir,
                                "video_${System.currentTimeMillis()}.mp4"
                            )
                            videoUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                videoFile
                            )
                            videoLauncher.launch(videoUri!!)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.video_recorder),
                            contentDescription = "Record Video",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            "Record Video",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    } else {
        Text("Camera permission required")
    }
}

//@Preview(showBackground = true)
//@Composable
//fun TaskToDoScreenPreview() {
//    TaskToDoScreen(
//        "123",
//        onBack = {}
//    )
//}