package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.newbiechen.inkreader.data.local.database.entities.AIConversationEntity

/**
 * AI Conversation Data Access Object
 * Provides database operations for AI conversation history
 */
@Dao
interface AIConversationDao {
    
    /**
     * Insert a new AI conversation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AIConversationEntity)
    
    /**
     * Get all conversations as Flow for reactive updates
     */
    @Query("SELECT * FROM ai_conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<AIConversationEntity>>
    
    /**
     * Get conversations for a specific book
     */
    @Query("SELECT * FROM ai_conversations WHERE book_id = :bookId ORDER BY timestamp DESC")
    fun getConversationsByBook(bookId: String): Flow<List<AIConversationEntity>>
    
    /**
     * Get conversations by question type
     */
    @Query("SELECT * FROM ai_conversations WHERE question_type = :questionType ORDER BY timestamp DESC")
    fun getConversationsByType(questionType: String): Flow<List<AIConversationEntity>>
    
    /**
     * Search conversations by text content
     */
    @Query("""
        SELECT * FROM ai_conversations 
        WHERE selected_text LIKE '%' || :searchQuery || '%' 
        OR answer LIKE '%' || :searchQuery || '%'
        ORDER BY timestamp DESC
    """)
    fun searchConversations(searchQuery: String): Flow<List<AIConversationEntity>>
    
    /**
     * Get conversation count
     */
    @Query("SELECT COUNT(*) FROM ai_conversations")
    suspend fun getConversationCount(): Int
    
    /**
     * Get conversations older than specified timestamp
     */
    @Query("SELECT * FROM ai_conversations WHERE timestamp < :timestamp ORDER BY timestamp ASC")
    suspend fun getConversationsOlderThan(timestamp: Long): List<AIConversationEntity>
    
    /**
     * Delete a specific conversation
     */
    @Delete
    suspend fun deleteConversation(conversation: AIConversationEntity)
    
    /**
     * Delete conversations by IDs
     */
    @Query("DELETE FROM ai_conversations WHERE id IN (:conversationIds)")
    suspend fun deleteConversationsByIds(conversationIds: List<String>)
    
    /**
     * Delete all conversations for a specific book
     */
    @Query("DELETE FROM ai_conversations WHERE book_id = :bookId")
    suspend fun deleteConversationsByBook(bookId: String)
    
    /**
     * Delete conversations older than specified timestamp
     */
    @Query("DELETE FROM ai_conversations WHERE timestamp < :timestamp")
    suspend fun deleteConversationsOlderThan(timestamp: Long)
    
    /**
     * Delete all conversations
     */
    @Query("DELETE FROM ai_conversations")
    suspend fun deleteAllConversations()
    
    /**
     * Get recent conversations (limited)
     */
    @Query("SELECT * FROM ai_conversations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentConversations(limit: Int = 20): Flow<List<AIConversationEntity>>
    
    /**
     * Get conversation by ID
     */
    @Query("SELECT * FROM ai_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): AIConversationEntity?
} 