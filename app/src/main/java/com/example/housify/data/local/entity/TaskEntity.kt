package com.example.housify.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.housify.data.remote.dto.TodayTask

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["userId"])]
)
data class TaskEntity(
    @PrimaryKey val taskId: String,
    val userId: String,
    val groupId: String,
    val taskInstanceId: String,
    val groupName: String,
    val title: String,
    val place: String,
    val scheduleType: String,
    val date: String,
)

fun TaskEntity.toDomain(): TodayTask {
    return TodayTask(
        groupId = groupId,
        taskId = taskId,
        taskInstanceId = taskInstanceId,
        groupName = groupName,
        title = title,
        place = place,
        scheduleType = scheduleType
    )
}