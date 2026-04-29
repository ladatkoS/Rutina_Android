package com.example.rutina_frontend.data.repository

import com.example.rutina_frontend.data.api.ApiService
import com.example.rutina_frontend.data.models.UserDto

class AdminRepository(
    private val apiService: ApiService
) {

    suspend fun getAllUsers(): Result<List<UserDto>> {
        return try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(userId: Long): Result<UserDto> {
        return try {
            val response = apiService.getUserInfo(userId)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}