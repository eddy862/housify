package com.example.housify.feature.profile

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.housify.feature.tasks.ImageGallery
import com.example.housify.feature.tasks.RatingComponent

@Composable
fun CompletedRatingsScreen(
    onBack: () -> Unit
) {
    val imageUrls = listOf(
        "https://hips.hearstapps.com/hmg-prod/images/cute-room-ideas-1677096334.png?crop=0.597xw:1.00xh;0.134xw,0&resize=640:*",
        "https://hips.hearstapps.com/hmg-prod/images/cute-room-ideas-1677096334.png?crop=0.597xw:1.00xh;0.134xw,0&resize=640:*",
        "https://hips.hearstapps.com/hmg-prod/images/cute-room-ideas-1677096334.png?crop=0.597xw:1.00xh;0.134xw,0&resize=640:*",
    )

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
                .background(Color.White)
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
                    // Date
                    Text(
                        "10 Oct 2025 3:00pm", style = TextStyle(
                            fontSize = 14.sp
                        )
                    )
                    // Assignee
                    Text(
                        "Assigned to Eddy", style = TextStyle(
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Clean the basin",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Group 1",
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
                        text = "Kitchen",
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

                Text("Comments", style = TextStyle(fontSize = 14.sp))

                Spacer(modifier = Modifier.height(10.dp))

                Rating(
                    title = "Cleanliness",
                    rating = 5,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Rating(
                    title = "Punctuality",
                    rating = 3,
                )

                Spacer(modifier = Modifier.height(20.dp))

//                ImageGallery(imageUrls)

                Spacer(modifier = Modifier.height(20.dp))

                Text("I am descdription", style = TextStyle(fontSize = 12.sp))
            }
        }
    }
}

@Composable
fun Rating(
    title: String,
    rating: Int,
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
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompletedRatingsScreenPreview() {
    CompletedRatingsScreen(
        { "123" },
    )
}