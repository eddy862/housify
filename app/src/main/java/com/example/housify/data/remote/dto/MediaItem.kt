package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaItem (
    val fileUrl: String,
    val mimeType: String
)