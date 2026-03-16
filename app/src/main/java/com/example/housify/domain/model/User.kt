package com.example.housify.domain.model

data class User(
    val id: String,
    val name: String,
    val isCurrentUser: Boolean = false
)