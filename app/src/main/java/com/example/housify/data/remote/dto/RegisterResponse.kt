package com.example.housify.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterResponse (
    val customToken: String
)