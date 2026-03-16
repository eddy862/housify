package com.example.housify.data.remote.dto

data class DownloadSignedUrl(
    val mimeType: String,
    val objectPath: String,
    val signedDownloadUrl: String
)