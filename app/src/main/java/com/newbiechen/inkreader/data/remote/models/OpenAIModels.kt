package com.newbiechen.inkreader.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * OpenAI Chat Completion Request Model
 */
data class ChatCompletionRequest(
    @SerializedName("model")
    val model: String = "gpt-3.5-turbo",
    
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000,
    
    @SerializedName("temperature")
    val temperature: Double = 0.7,
    
    @SerializedName("top_p")
    val topP: Double = 1.0,
    
    @SerializedName("frequency_penalty")
    val frequencyPenalty: Double = 0.0,
    
    @SerializedName("presence_penalty")
    val presencePenalty: Double = 0.0
)

/**
 * Chat Message Model
 */
data class ChatMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    
    @SerializedName("content")
    val content: String
)

/**
 * OpenAI Chat Completion Response Model
 */
data class ChatCompletionResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("object")
    val `object`: String,
    
    @SerializedName("created")
    val created: Long,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("choices")
    val choices: List<Choice>,
    
    @SerializedName("usage")
    val usage: Usage?
)

/**
 * Choice Model
 */
data class Choice(
    @SerializedName("index")
    val index: Int,
    
    @SerializedName("message")
    val message: ChatMessage,
    
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * Usage Model
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * OpenAI Error Response Model
 */
data class OpenAIErrorResponse(
    @SerializedName("error")
    val error: OpenAIError
)

data class OpenAIError(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("param")
    val param: String?,
    
    @SerializedName("code")
    val code: String?
) 