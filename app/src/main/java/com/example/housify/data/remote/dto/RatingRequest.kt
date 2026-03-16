package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RatingRequest (
    val cleanlinessScore: Int,
    val punctualityScore: Int,
    val comment: String
)