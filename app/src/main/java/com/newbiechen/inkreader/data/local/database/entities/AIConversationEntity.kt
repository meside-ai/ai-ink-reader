package com.newbiechen.inkreader.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newbiechen.inkreader.core.ai.models.AIQuestionType

/**
 * AI Conversation Entity for Database Storage
 * Stores AI conversation history with relationship to books
 */
@Entity(
    tableName = "ai_conversations",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["question_type"])
    ]
)
data class AIConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "book_title")
    val bookTitle: String,
    
    @ColumnInfo(name = "chapter_title")
    val chapterTitle: String?,
    
    @ColumnInfo(name = "selected_text")
    val selectedText: String,
    
    @ColumnInfo(name = "question_type")
    val questionType: String, // Store as String for Room compatibility
    
    @ColumnInfo(name = "answer")
    val answer: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "is_from_voice")
    val isFromVoice: Boolean = false
) {
    /**
     * Convert string back to enum
     */
    fun getQuestionTypeEnum(): AIQuestionType {
        return AIQuestionType.valueOf(questionType)
    }
} 