package com.newbiechen.inkreader.data.local.database.entities

import androidx.room.*
import com.newbiechen.inkreader.domain.entities.AIConversation
import com.newbiechen.inkreader.domain.entities.QuestionType

/**
 * AI对话数据库实体类
 * 
 * 存储用户与AI助手的对话记录
 */
@Entity(
    tableName = "ai_conversations",
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["book_id"]),
        Index(value = ["created_at"]),
        Index(value = ["question_type"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AIConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "selected_text")
    val selectedText: String,
    
    @ColumnInfo(name = "context")
    val context: String,
    
    @ColumnInfo(name = "question_type")
    val questionType: String, // 存储为字符串，通过转换器处理
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

/**
 * AI回答实体类
 */
@Entity(
    tableName = "ai_responses",
    indices = [
        Index(value = ["response_id"]),
        Index(value = ["conversation_id"]),
        Index(value = ["content_hash"]),
        Index(value = ["created_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = AIConversationEntity::class,
            parentColumns = ["conversation_id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AIResponseEntity(
    @PrimaryKey
    @ColumnInfo(name = "response_id")
    val responseId: String,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "content_hash")
    val contentHash: String, // 用于缓存查重
    
    @ColumnInfo(name = "token_count")
    val tokenCount: Int = 0,
    
    @ColumnInfo(name = "response_time")
    val responseTime: Long = 0, // API响应时间（毫秒）
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

/**
 * 扩展函数：Entity转换为Domain对象
 */
fun AIConversationEntity.toDomain(): AIConversation {
    return AIConversation(
        conversationId = conversationId,
        bookId = bookId,
        selectedText = selectedText,
        context = context,
        questionType = QuestionType.valueOf(questionType),
        createdAt = createdAt
    )
}

/**
 * 扩展函数：Domain对象转换为Entity
 */
fun AIConversation.toEntity(): AIConversationEntity {
    return AIConversationEntity(
        conversationId = conversationId,
        bookId = bookId,
        selectedText = selectedText,
        context = context,
        questionType = questionType.name,
        createdAt = createdAt
    )
} 