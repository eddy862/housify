package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
@Serializable
data class FutureTask (
    val taskId: String,
    val groupName: String,
    val title: String,
    val place: String,
    val scheduleType: String
)