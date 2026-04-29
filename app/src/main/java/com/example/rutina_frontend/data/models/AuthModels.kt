package com.example.rutina_frontend.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val username: String,
    val password: String,
    val phone: String
)

data class AuthResponse(
    val token: String,
    val username: String
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val balance: Int = 0,
    val totalScore: Int = 0,
    val countOfHabits: Int = 0,
    val role: String,
    val createdAt: String
)