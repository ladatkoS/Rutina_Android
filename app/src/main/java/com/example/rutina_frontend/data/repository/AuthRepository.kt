package com.example.rutina_frontend.data.repository

import com.example.rutina_frontend.data.api.ApiService
import com.example.rutina_frontend.data.models.AuthResponse
import com.example.rutina_frontend.data.models.LoginRequest
import com.example.rutina_frontend.data.models.RegisterRequest
import com.example.rutina_frontend.data.models.UserDto

class AuthRepository(
    private val apiService: ApiService
) {

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        username: String,
        password: String,
        phone: String
    ): Result<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(name, username, password, phone))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateToken(): Result<UserDto> {
        return try {
            val response = apiService.validateToken()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Invalid token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}