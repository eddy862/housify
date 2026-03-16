package com.example.housify.data.remote.dto

import com.example.housify.convertMillisToDate
import com.example.housify.domain.model.PredefinedSpace
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.Task
import com.example.housify.domain.model.TaskSchedule
import com.example.housify.domain.model.User
import com.squareup.moshi.JsonClass

// dto from api
@JsonClass(generateAdapter = true)
data class TaskDto(
    val taskId: String,
    val groupId: String,
    val title: String,
    val place: String,
    val assigneeIds: List<String>,
    val taskSchedule: TaskScheduleDto,
    val isDeleted: Boolean,
    val createdAt: Long,
    val createdBy: String
)

enum class ScheduleTypeEnumDto {
    ONCE,
    DAILY,
    WEEKLY
}

@JsonClass(generateAdapter = true)
data class TaskScheduleDto(
    val scheduleType: ScheduleTypeEnumDto,
    val startDate: Long,
    val isRotational: Boolean
)

enum class TaskStatusEnumDto {
    COMPLETED,
    PENDING,
    OVERDUE
}

@JsonClass(generateAdapter = true)
data class TaskInstanceDto(
    val taskId: String,
    val groupId: String,
    val scheduleType: ScheduleTypeEnumDto,
    val date: Long,
    val assigneeId: String,
    val taskStatus: TaskStatusEnumDto
)

fun TaskInstanceDto.toDomain(
    groupMembers: List<User>,
): TaskSchedule {
    return TaskSchedule(
        user = groupMembers.first { it.id == assigneeId },
        date = convertMillisToDate(date, true)
    )
}

data class FutureTaskDto(
    val taskId: String,
    val groupName: String,
    val title: String,
    val place: String,
    val scheduleType: ScheduleTypeEnumDto,
)

// mapper functions
fun TaskDto.toDomain(groupMembers: List<User>, currentUserId: String): Task {
    return Task(
        id = taskId,
        groupId = groupId,
        title = title,
        startDate = convertMillisToDate(taskSchedule.startDate, true),
        repeat = when (taskSchedule.scheduleType) {
            ScheduleTypeEnumDto.ONCE -> Repeat.NONE
            ScheduleTypeEnumDto.DAILY -> Repeat.DAILY
            ScheduleTypeEnumDto.WEEKLY -> Repeat.WEEKLY
        },
        rotationEnabled = taskSchedule.isRotational,
        space = PredefinedSpace.getSortedList()
            .firstOrNull { it.name.equals(place, ignoreCase = true) }
            ?: throw Exception("Invalid space name"),
        assignees = groupMembers.filter { it.id in assigneeIds },
        assignedToCurrentUser = currentUserId in assigneeIds
    )
}