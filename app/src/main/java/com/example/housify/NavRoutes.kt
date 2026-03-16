package com.example.housify

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

// navigation routes
@Serializable
object Auth // login & signup

// main screens with bottom bar
@Serializable
object Tasks

@Serializable
object Groups

@Serializable
object Profile

// sub-screens for Tasks
@Serializable
data class TaskToDo(
    val groupId: String,
    val taskId: String,
    val taskInstanceId: String,
    val groupName: String,
    val title: String,
    val place: String,
    val scheduleType: String
)

@Serializable
data class RatingToDo(
    val reviewId: String,
    val groupId: String,
    val groupName: String,
    val revieweeName: String,
    val title: String,
    val place: String,
    val reviewCreatedAt: Long
)

// sub-screens for Groups
@Serializable
data class GroupEntry(val groupId: String)

@Serializable
data class LeaderboardHistory(val groupId: String)

@Serializable
data class LeaderboardHistoryEntry(val groupId: String, val week: Int)

@Serializable
data class CreateTask(val groupId: String)

@Serializable
data class TaskDetails(val groupId: String, val taskId: String)

// sub-screens for Profile
@Serializable
object CompletedTasks

@Serializable
object CompletedRatings

// screen for joining group via scanning qr
@Serializable
data class JoinGroup(val invitationCode: String)

// screen to update host ip
@Serializable
object UpdateHostIp

const val joinGroupURI = "housify://join-group/{invitationCode}"

// the navigation items within the bottom nav bar
data class BottomNavItem(
    val label: String,
    @DrawableRes val icon: Int,
    // icon
    val route: Any
) {
    fun isSelected(currentRoute: String): Boolean = route::class.qualifiedName == currentRoute
}

data class TopNavItem(
    val title: String,
    val route: Any,
)

val bottomNavItems = listOf(
    BottomNavItem("Tasks", R.drawable.nav_tasks, Tasks),
    BottomNavItem("Groups", R.drawable.nav_groups, Groups),
    BottomNavItem("Profile", R.drawable.nav_profile, Profile)
)

val topNavItems = listOf(
    TopNavItem("Group", GroupEntry),
    TopNavItem("Task Details", TaskDetails),
    TopNavItem("Create Task", CreateTask),
    TopNavItem("Leaderboard History", LeaderboardHistory),
    TopNavItem("Leaderboard", LeaderboardHistoryEntry),
    TopNavItem("Complete Task", TaskToDo),
    TopNavItem("Peer Rating", RatingToDo),
    TopNavItem("Completed Tasks", CompletedTasks),
    TopNavItem("Completed Ratings", CompletedRatings),
    TopNavItem("Update Host IP", UpdateHostIp)
)
