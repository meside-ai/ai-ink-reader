package com.newbiechen.inkreader.core.ai.models

import com.newbiechen.inkreader.data.remote.models.Usage

/**
 * AI Question Types
 * Represents the five core question scenarios
 */
enum class AIQuestionType(val displayName: String, val description: String) {
    CONCEPT_EXPLANATION("概念解释", "解释文本中的核心概念和知识点"),
    TRANSLATION("翻译", "中英文互译"),
    WORD_LEARNING("单词学习", "英语词汇和语法学习指导"),
    SUMMARY("总结", "提取要点，简洁总结内容"),
    EXPANSION("扩展阅读", "推荐相关学习方向和话题")
}

/**
 * AI Question Request Model
 * Contains all information needed to make an AI question
 */
data class AIQuestionRequest(
    val selectedText: String,
    val context: String? = null,
    val questionType: AIQuestionType,
    val bookId: String? = null,
    val chapterTitle: String? = null
)

/**
 * AI Question Response Model
 * Contains the AI's response and metadata
 */
data class AIQuestionResponse(
    val id: String,
    val questionType: AIQuestionType,
    val selectedText: String,
    val answer: String,
    val timestamp: Long,
    val usage: Usage? = null
)

/**
 * AI Conversation Entry for History Management
 * Represents a single conversation entry that can be stored locally
 */
data class AIConversationEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val bookId: String,
    val bookTitle: String,
    val chapterTitle: String? = null,
    val selectedText: String,
    val questionType: AIQuestionType,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromVoice: Boolean = false
)

/**
 * AI Question Result for UI State Management
 * Wraps the response with loading and error states
 */
sealed class AIQuestionResult {
    object Loading : AIQuestionResult()
    data class Success(val response: AIQuestionResponse) : AIQuestionResult()
    data class Error(val message: String, val throwable: Throwable? = null) : AIQuestionResult()
} 