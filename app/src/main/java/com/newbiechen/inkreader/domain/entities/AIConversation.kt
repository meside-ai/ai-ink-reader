package com.newbiechen.inkreader.domain.entities

import java.util.UUID

/**
 * AI对话实体类
 * 
 * 表示用户与AI助手的一次对话信息
 */
data class AIConversation(
    val conversationId: String = UUID.randomUUID().toString(),
    val bookId: String,
    val selectedText: String,
    val context: String,
    val questionType: QuestionType,
    val createdAt: Long = System.currentTimeMillis()
) {
    
    /**
     * 获取选中文本的摘要（用于显示）
     */
    fun getSelectedTextSummary(maxLength: Int = 50): String {
        return if (selectedText.length <= maxLength) {
            selectedText
        } else {
            selectedText.take(maxLength) + "..."
        }
    }
    
    /**
     * 检查对话是否有效
     */
    fun isValid(): Boolean {
        return bookId.isNotBlank() && 
               selectedText.isNotBlank()
    }
}

/**
 * AI问题类型枚举
 */
enum class QuestionType(val displayName: String, val promptTemplate: String) {
    CONCEPT_EXPLANATION("概念解释", "请解释以下概念或术语的含义："),
    TRANSLATION("中英翻译", "请将以下文本进行中英文翻译："),
    VOCABULARY_LEARNING("英语学习", "请解释这个英语单词或短语，包括发音、含义和用法："),
    CONTENT_SUMMARY("内容总结", "请总结以下内容的要点："),
    EXTENDED_READING("扩展阅读", "请推荐与以下内容相关的扩展阅读或知识点："),
    CUSTOM("自定义问题", "")
} 