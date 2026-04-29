package com.example.rutina_frontend.data.repository

import com.example.rutina_frontend.data.api.ApiService
import com.example.rutina_frontend.data.models.CreateHabitRequest
import com.example.rutina_frontend.data.models.HabitDto

class HabitRepository(
    private val apiService: ApiService
) {

    suspend fun createHabit(
        name: String,
        description: String,
        type: String,
        formationPeriod: Int
    ): Result<HabitDto> {
        return try {
            val response = apiService.createHabit(
                CreateHabitRequest(name, description, type, formationPeriod)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create habit"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabits(): Result<List<HabitDto>> {
        return try {
            val response = apiService.getHabits()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch habits"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteHabit(habitId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteHabit(habitId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete habit"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}