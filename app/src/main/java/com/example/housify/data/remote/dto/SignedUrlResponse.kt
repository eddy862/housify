package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignedUrlResponse (
    val mimeType: String,
    val objectPath: String,
    val signedUploadUrl: String
)