package com.example.rutina_frontend.di

import android.content.Context
import com.example.rutina_frontend.data.api.AiApiService
import com.example.rutina_frontend.data.api.ApiService
import com.example.rutina_frontend.data.api.AuthInterceptor
import com.example.rutina_frontend.data.repository.AdminRepository
import com.example.rutina_frontend.data.repository.AuthRepository
import com.example.rutina_frontend.data.repository.HabitRepository
import com.example.rutina_frontend.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceLocator {

    private var apiService: ApiService? = null
    private var aiApiService: AiApiService? = null
    private var authRepository: AuthRepository? = null
    private var habitRepository: HabitRepository? = null
    private var adminRepository: AdminRepository? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptor(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Основной API
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // AI API (отдельный клиент без авторизации)
        val aiOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val aiRetrofit = Retrofit.Builder()
            .baseUrl(Constants.AI_BASE_URL)
            .client(aiOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        aiApiService = aiRetrofit.create(AiApiService::class.java)

        isInitialized = true
    }

    fun getApiService(): ApiService {
        return apiService ?: throw IllegalStateException("ServiceLocator must be initialized first!")
    }

    fun getAiApiService(): AiApiService {
        return aiApiService ?: throw IllegalStateException("ServiceLocator must be initialized first!")
    }

    fun getAuthRepository(): AuthRepository {
        if (authRepository == null) {
            authRepository = AuthRepository(getApiService())
        }
        return authRepository!!
    }

    fun getHabitRepository(): HabitRepository {
        if (habitRepository == null) {
            habitRepository = HabitRepository(getApiService())
        }
        return habitRepository!!
    }

    fun getAdminRepository(): AdminRepository {
        if (adminRepository == null) {
            adminRepository = AdminRepository(getApiService())
        }
        return adminRepository!!
    }
}