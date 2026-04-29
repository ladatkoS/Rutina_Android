package com.example.rutina_frontend.data.api

import com.example.rutina_frontend.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/validate")
    suspend fun validateToken(): Response<UserDto>

    @GET("auth/user/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserDto>

    @POST("users/createHabits")
    suspend fun createHabit(@Body request: CreateHabitRequest): Response<HabitDto>

    @GET("users/getHabits")
    suspend fun getHabits(): Response<List<HabitDto>>

    @GET("users/getHabits/{habitId}")
    suspend fun getHabitById(@Path("habitId") habitId: Long): Response<HabitDto>

    @DELETE("users/deleteHabits/{habitId}")
    suspend fun deleteHabit(@Path("habitId") habitId: Long): Response<Unit>

    @GET("admin/allusers")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @GET("admin/info/{id}")
    suspend fun getUserInfo(@Path("id") id: Long): Response<UserDto>

    @DELETE("admin/delete/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>
}