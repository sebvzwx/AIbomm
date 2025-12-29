package com.example.aibomm.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}
