package com.example.housify.domain.model

import androidx.annotation.DrawableRes
import com.example.housify.R
import com.example.housify.data.remote.dto.ScheduleTypeEnumDto

class Space(val name: String, @DrawableRes val icon: Int, val order: Int)

object PredefinedSpace {
    val Bathroom = Space("bathroom", R.drawable.bathroom, 1)
    val Kitchen = Space("kitchen", R.drawable.kitchen, 2)
    val LivingRoom = Space("living room", R.drawable.living, 3)
    val DiningRoom = Space("dining room", R.drawable.dining, 4)
    val LaundryRoom = Space("laundry room", R.drawable.laundry, 5)
    val Other = Space("other", R.drawable.other, 6)

    private val allSpaces = listOf(Bathroom, Kitchen, LivingRoom, DiningRoom, LaundryRoom, Other)

    fun getSortedList(): List<Space> {
        return allSpaces.sortedBy { it.order }
    }
}

enum class Repeat(val displayText: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
}

fun Repeat.toDto(): ScheduleTypeEnumDto {
    return when (this) {
        Repeat.NONE -> ScheduleTypeEnumDto.ONCE
        Repeat.DAILY -> ScheduleTypeEnumDto.DAILY
        Repeat.WEEKLY -> ScheduleTypeEnumDto.WEEKLY
    }
}

data class TaskDetails(
    val task: Task,
    val isUserAdmin: Boolean,
    val recentSchedule: List<TaskSchedule>
)

data class TaskSchedule(
    val user: User,
    val date: String,
)

// if no repeat and multiple assign,
data class Task(
    val id: String,
    val groupId: String,
    val title: String,
    val startDate: String,
    val repeat: Repeat,
    val rotationEnabled: Boolean,
    val space: Space,
    val assignees: List<User>,
    val assignedToCurrentUser: Boolean
)
