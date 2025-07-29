package com.newbiechen.inkreader.data.remote.api

import com.newbiechen.inkreader.data.remote.models.ChatCompletionRequest
import com.newbiechen.inkreader.data.remote.models.ChatCompletionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenAI API Service Interface
 * Defines endpoints for OpenAI API calls
 */
interface OpenAIApiService {
    
    /**
     * Chat Completion API endpoint
     * Used for text-based AI questions and answers
     */
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
    
    companion object {
        const val BASE_URL = "https://api.openai.com/"
        const val AUTHORIZATION_PREFIX = "Bearer "
    }
} 