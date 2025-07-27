package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.newbiechen.inkreader.data.local.database.entities.AIConversationEntity
import com.newbiechen.inkreader.data.local.database.entities.AIResponseEntity

/**
 * AI对话数据访问对象
 * 
 * 管理用户与AI助手的对话记录和响应缓存
 */
@Dao
interface AIConversationDao {
    
    /**
     * 获取图书的所有AI对话记录（响应式）
     */
    @Query("""
        SELECT * FROM ai_conversations 
        WHERE book_id = :bookId 
        ORDER BY created_at DESC
    """)
    fun getConversationsByBookIdFlow(bookId: String): Flow<List<AIConversationEntity>>
    
    /**
     * 获取图书的所有AI对话记录
     */
    @Query("""
        SELECT * FROM ai_conversations 
        WHERE book_id = :bookId 
        ORDER BY created_at DESC
    """)
    suspend fun getConversationsByBookId(bookId: String): List<AIConversationEntity>
    
    /**
     * 根据对话ID获取对话记录
     */
    @Query("SELECT * FROM ai_conversations WHERE conversation_id = :conversationId")
    suspend fun getConversationById(conversationId: String): AIConversationEntity?
    
    /**
     * 获取所有对话记录
     */
    @Query("""
        SELECT * FROM ai_conversations 
        ORDER BY created_at DESC
    """)
    suspend fun getAllConversations(): List<AIConversationEntity>
    
    /**
     * 获取最近的对话记录
     */
    @Query("""
        SELECT * FROM ai_conversations 
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun getRecentConversations(limit: Int): List<AIConversationEntity>
    
    /**
     * 插入对话记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AIConversationEntity): Long
    
    /**
     * 批量插入对话记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<AIConversationEntity>): List<Long>
    
    /**
     * 删除对话记录
     */
    @Delete
    suspend fun deleteConversation(conversation: AIConversationEntity): Int
    
    /**
     * 根据图书ID删除所有对话
     */
    @Query("DELETE FROM ai_conversations WHERE book_id = :bookId")
    suspend fun deleteConversationsByBookId(bookId: String): Int
    
    /**
     * 清理老旧的对话记录（超过指定天数）
     */
    @Query("""
        DELETE FROM ai_conversations 
        WHERE created_at < :cutoffTime
    """)
    suspend fun cleanupOldConversations(cutoffTime: Long): Int
    
    /**
     * 根据问题类型搜索对话
     */
    @Query("""
        SELECT * FROM ai_conversations 
        WHERE question_type = :questionType 
        ORDER BY created_at DESC
    """)
    suspend fun getConversationsByType(questionType: String): List<AIConversationEntity>
    
    /**
     * 搜索对话内容
     */
    @Query("""
        SELECT * FROM ai_conversations 
        WHERE selected_text LIKE '%' || :query || '%' 
        ORDER BY created_at DESC
    """)
    suspend fun searchConversations(query: String): List<AIConversationEntity>
    
    /**
     * 获取对话统计信息
     */
    @Query("""
        SELECT 
            COUNT(*) as total_conversations,
            COUNT(DISTINCT book_id) as books_with_conversations,
            COUNT(DISTINCT question_type) as question_types_used
        FROM ai_conversations
    """)
    suspend fun getConversationStatistics(): ConversationStatistics?
}

/**
 * AI回答数据访问对象
 */
@Dao
interface AIResponseDao {
    
    /**
     * 根据对话ID获取回答
     */
    @Query("SELECT * FROM ai_responses WHERE conversation_id = :conversationId")
    suspend fun getResponseByConversationId(conversationId: String): AIResponseEntity?
    
    /**
     * 根据内容哈希查找缓存的回答
     */
    @Query("SELECT * FROM ai_responses WHERE content_hash = :contentHash LIMIT 1")
    suspend fun getCachedResponseByHash(contentHash: String): AIResponseEntity?
    
    /**
     * 插入AI回答
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: AIResponseEntity): Long
    
    /**
     * 更新回答
     */
    @Update
    suspend fun updateResponse(response: AIResponseEntity): Int
    
    /**
     * 删除回答
     */
    @Delete
    suspend fun deleteResponse(response: AIResponseEntity): Int
    
    /**
     * 根据对话ID删除回答
     */
    @Query("DELETE FROM ai_responses WHERE conversation_id = :conversationId")
    suspend fun deleteResponseByConversationId(conversationId: String): Int
    
    /**
     * 清理老旧的缓存回答
     */
    @Query("""
        DELETE FROM ai_responses 
        WHERE created_at < :cutoffTime
    """)
    suspend fun cleanupOldResponses(cutoffTime: Long): Int
    
    /**
     * 获取回答统计信息
     */
    @Query("""
        SELECT 
            COUNT(*) as total_responses,
            AVG(token_count) as avg_token_count,
            AVG(response_time) as avg_response_time
        FROM ai_responses
    """)
    suspend fun getResponseStatistics(): ResponseStatistics?
}

/**
 * 对话统计数据类
 */
data class ConversationStatistics(
    val totalConversations: Int,
    val booksWithConversations: Int,
    val questionTypesUsed: Int
)

/**
 * 回答统计数据类
 */
data class ResponseStatistics(
    val totalResponses: Int,
    val avgTokenCount: Float,
    val avgResponseTime: Float
) 