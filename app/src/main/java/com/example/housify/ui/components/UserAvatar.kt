package com.example.housify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.housify.ui.theme.HousifyTheme

@Composable
fun UserAvatar(
    username: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    border: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.onPrimary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {}
) {
    val initials = username.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (border) Modifier.border(2.dp, borderColor, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = (size.value / 2.5).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun UserAvatarPreview() {
    HousifyTheme {
        UserAvatar(username = "John Doe")
    }
}

@Preview(showBackground = true)
@Composable
fun UserAvatarSingleNamePreview() {
    HousifyTheme {
        UserAvatar(username = "Alice")
    }
}

@Preview
@Composable
fun UserAvatarThreePreview() {
    HousifyTheme {
        UserAvatar(username = "John Doe Jordan")
    }
}

