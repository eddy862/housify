package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class TaskScheduleReq(
    val scheduleType: ScheduleTypeEnumDto,
    val startDate: LocalDate,
    val isRotational: Boolean
)

@JsonClass(generateAdapter = true)
data class CreateTaskReq(
    val title: String,
    val place: String,
    val assigneeIds: List<String>,
    val taskSchedule: TaskScheduleReq
)