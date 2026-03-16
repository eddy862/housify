package com.example.housify.feature.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.housify.R
import com.example.housify.data.local.datastore.ThemePreferenceManager
import com.example.housify.feature.auth.AuthViewModel
import com.example.housify.ui.components.SimpleDialog
import com.example.housify.ui.components.UserAvatar
import com.example.housify.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onSelectCompletedTasks: () -> Unit,
    onSelectCompletedRatings: () -> Unit,
    onSuccessLogout: () -> Unit,
    currentTheme: AppTheme,
    onSwitchTheme: (AppTheme) -> Unit,
    onEnterUpdateIpScreen: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var isEditDialogOpen by remember { mutableStateOf(false) }
    val userState by profileViewModel.user.collectAsState()
    val userStat by profileViewModel.userStat.collectAsState()

    val scope = rememberCoroutineScope()

    val usernameError = profileViewModel.usernameError
    val isUpdatingUsername = profileViewModel.isUpdatingUsername

    when {
        isEditDialogOpen -> {
            SimpleDialog(
                onDismissRequest = {
                    isEditDialogOpen = false
                    profileViewModel.clearUsernameError()
                },
                inputPrefixId = R.drawable.group_name,
                inputPrefixContentDesc = "profile name",
                inputPlaceholder = "Profile name",
                onConfirm = { newUsername ->
                    profileViewModel.updateUsername(newUsername) {
                        // only close when success
                        isEditDialogOpen = false
                    }
                },
                buttonText = "Save",
                loading = isUpdatingUsername,
                error = usernameError.orEmpty()
            )
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.getUserStat()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(horizontal = 5.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    authViewModel.signOut()
                    onSuccessLogout()
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.logout),
                    contentDescription = "logout",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        ProfileHeader(
            userState?.profileUrl, userState?.username ?: "-",
            { isEditDialogOpen = true }
        )

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
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Completed Tasks",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    val totalCompleted = userStat?.totalCompletedTasksCount ?: 0
                    val totalOverdue = userStat?.totalOverdueTasksCount ?: 0
                    val totalTasks = totalCompleted + totalOverdue
                    val taskCompletedPercentage =
                        if (totalTasks == 0) 0 else (totalCompleted * 100 / totalTasks)

                    TaskCompletedPercentage(taskCompletedPercentage)

                    Column() {
                        StatComponent("Completed", totalCompleted, Color(0xFF61C189))
                        Spacer(modifier = Modifier.height(4.dp))
                        StatComponent(
                            "Overdue",
                            totalOverdue,
                            MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TotalReceivedTaskStatComponent(
                            "Total Received Tasks",
                            totalTasks,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "View completed tasks >",
                        color = MaterialTheme.colorScheme.primary,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                        modifier = Modifier
                            .clickable { onSelectCompletedTasks() }
                    )
                }
            }
        }

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
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Completed Ratings",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ratingDetail(
                        (userStat?.totalCompletedReviewCount ?: 0).toString(), "Total",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Theme Setting")

            ThemeSetting(
                currentTheme = currentTheme,
                onThemeSelected = onSwitchTheme
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        AppNameAndVersion(
            content = "Housify @ Version 1.0.0",
            onEnterUpdateIpScreen = onEnterUpdateIpScreen
        )
    }
}

@Composable
fun AppNameAndVersion(
    content: String,
    onEnterUpdateIpScreen: () -> Unit,
    targetClick: Int = 3,
) {
    var clickCount by remember { mutableIntStateOf(0) }

    Text(
        text = content,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable {
                clickCount++
                if (clickCount == targetClick) {
                    onEnterUpdateIpScreen()
                    clickCount = 0
                }
            },
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun ProfileHeader(
    imageUrl: String? = null,
    name: String,
    onEditClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {

        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            // Profile Circle
            UserAvatar(
                username = name,
                size = 120.dp,
            )

            // Edit icon bubble
            Box(
                modifier = Modifier
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        onEditClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
fun ratingDetail(
    num: String, title: String, modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            num, style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            title, style = TextStyle(
                fontSize = 14.sp
            )
        )
    }
}

@Composable
fun TopIconButton(
    onClick: () -> Unit,
    @DrawableRes resId: Int,
    contentDesc: String,
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(resId),
            contentDescription = contentDesc,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun TaskCompletedPercentage(
    progressPercent: Int,           // e.g. 60
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    color: Color = Color(0xFF12B76A),
    backgroundColor: Color = MaterialTheme.colorScheme.secondary
) {
    val progress = progressPercent.coerceIn(0, 100) / 100f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {

        Canvas(modifier = Modifier.size(size)) {

            // Background arc
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$progressPercent%",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatComponent(title: String, num: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                num.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TotalReceivedTaskStatComponent(title: String, num: Int) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                num.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ThemeSetting(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        ThemeOption(
            title = "Light",
            icon = Icons.Default.WbSunny,
            isSelected = currentTheme == AppTheme.LIGHT,
            onClick = { onThemeSelected(AppTheme.LIGHT) },
            modifier = Modifier.weight(1f)
        )

        ThemeOption(
            title = "Dark",
            icon = Icons.Default.DarkMode,
            isSelected = currentTheme == AppTheme.DARK,
            onClick = { onThemeSelected(AppTheme.DARK) },
            modifier = Modifier.weight(1f)
        )

        ThemeOption(
            title = "Default",
            icon = Icons.Default.Settings,
            isSelected = currentTheme == AppTheme.SYSTEM,
            onClick = { onThemeSelected(AppTheme.SYSTEM) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Black

    Column(
        modifier = modifier
//            .border(1.dp, MaterialTheme.colorScheme.secondary)
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = textColor)
        Text(title, color = textColor)
    }
}


//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    ProfileScreen(
//        onSelectCompletedTasks = {},
//        onSelectCompletedRatings = {},
//        onSuccessLogout = {}
//    )
//}