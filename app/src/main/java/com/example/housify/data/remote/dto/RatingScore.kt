package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RatingScore(
    val name: String,
    val cleanlinessScore: Int,
    val punctualityScore: Int,
    val comment: String,
    val createdAt: Long
)