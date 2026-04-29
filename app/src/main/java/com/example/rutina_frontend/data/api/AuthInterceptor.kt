package com.example.rutina_frontend.data.api

import android.content.Context
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val dataStoreManager = DataStoreManager(context)

        val token = runBlocking {
            dataStoreManager.getToken()
        }

        println("🔑 INTERCEPTOR - Token from storage: ${token?.take(30)}...")

        val request = if (!token.isNullOrEmpty()) {
            val cleanToken = token.replace("Bearer ", "").trim()
            println("🔑 INTERCEPTOR - Adding Authorization header with token: ${cleanToken.take(30)}...")

            originalRequest.newBuilder()
                .header("Authorization", "Bearer $cleanToken")
                .method(originalRequest.method, originalRequest.body)
                .build()
        } else {
            println("⚠️ INTERCEPTOR - No token found, proceeding without Authorization")
            originalRequest
        }

        return chain.proceed(request)
    }
}