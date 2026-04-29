package com.example.rutina_frontend.data.models

import com.google.gson.annotations.SerializedName

data class HabitDto(
    val id: Long,
    val name: String,
    val description: String,
    val type: String,
    val formationPeriod: Int,
    val createdAt: String,
    val endedAt: String,
    val userId: Long
)

data class CreateHabitRequest(
    val name: String,
    val description: String,
    val type: String,
    val formationPeriod: Int
)