package com.example.rutina_frontend.data.api

import com.example.rutina_frontend.data.models.AdviceRequest
import com.example.rutina_frontend.data.models.AdviceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {

    @POST("advice")
    suspend fun getAdvice(@Body request: AdviceRequest): Response<AdviceResponse>
}