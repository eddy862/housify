package com.example.housify.data.remote.dto

import com.example.housify.data.local.entity.TaskEntity
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
data class TodayTask(
    val groupId: String,
    val taskId: String,
    val taskInstanceId: String,
    val groupName: String,
    val title: String,
    val place: String,
    val scheduleType: String
)

fun TodayTask.toEntity(userId: String, date: String): TaskEntity {
    return TaskEntity(
        userId = userId,
        groupId = groupId,
        taskId = taskId,
        taskInstanceId = taskInstanceId,
        title = title,
        place = place,
        scheduleType = scheduleType,
        groupName = groupName,
        date = date
    )
}