package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.newbiechen.inkreader.data.local.database.entities.ReadingProgressEntity

/**
 * 阅读进度数据访问对象
 * 
 * 管理用户在每本书的阅读进度和位置信息
 */
@Dao
interface ReadingProgressDao {
    
    /**
     * 根据图书ID获取阅读进度（响应式）
     */
    @Query("SELECT * FROM reading_progress WHERE book_id = :bookId")
    fun getProgressByBookIdFlow(bookId: String): Flow<ReadingProgressEntity?>
    
    /**
     * 根据图书ID获取阅读进度
     */
    @Query("SELECT * FROM reading_progress WHERE book_id = :bookId")
    suspend fun getProgressByBookId(bookId: String): ReadingProgressEntity?
    
    /**
     * 获取所有有进度的图书
     */
    @Query("""
        SELECT * FROM reading_progress 
        ORDER BY last_updated DESC
    """)
    suspend fun getAllProgress(): List<ReadingProgressEntity>
    
    /**
     * 获取最近阅读的进度记录
     */
    @Query("""
        SELECT * FROM reading_progress 
        ORDER BY last_updated DESC 
        LIMIT :limit
    """)
    suspend fun getRecentProgress(limit: Int): List<ReadingProgressEntity>
    
    /**
     * 插入或更新阅读进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ReadingProgressEntity): Long
    
    /**
     * 更新阅读进度
     */
    @Update
    suspend fun updateProgress(progress: ReadingProgressEntity): Int
    
    /**
     * 批量更新进度百分比
     */
    @Query("""
        UPDATE reading_progress 
        SET 
            chapter_index = :chapterIndex,
            paragraph_index = :paragraphIndex,
            character_offset = :characterOffset,
            progress_percent = :progressPercent,
            last_updated = :timestamp
        WHERE book_id = :bookId
    """)
    suspend fun updateProgressDetails(
        bookId: String,
        chapterIndex: Int,
        paragraphIndex: Int,
        characterOffset: Int,
        progressPercent: Float,
        timestamp: Long
    ): Int
    
    /**
     * 更新进度百分比
     */
    @Query("""
        UPDATE reading_progress 
        SET progress_percent = :progressPercent, last_updated = :timestamp 
        WHERE book_id = :bookId
    """)
    suspend fun updateProgressPercent(bookId: String, progressPercent: Float, timestamp: Long): Int
    
    /**
     * 删除阅读进度
     */
    @Delete
    suspend fun deleteProgress(progress: ReadingProgressEntity): Int
    
    /**
     * 根据图书ID删除进度
     */
    @Query("DELETE FROM reading_progress WHERE book_id = :bookId")
    suspend fun deleteProgressByBookId(bookId: String): Int
    
    /**
     * 清理所有阅读进度
     */
    @Query("DELETE FROM reading_progress")
    suspend fun clearAllProgress(): Int
    
    /**
     * 获取阅读进度统计
     */
    @Query("""
        SELECT 
            COUNT(*) as total_books_with_progress,
            AVG(progress_percent) as average_progress,
            MAX(progress_percent) as max_progress,
            MIN(progress_percent) as min_progress
        FROM reading_progress
    """)
    suspend fun getProgressStatistics(): ProgressStatistics?
    
    /**
     * 获取完成阅读的图书数量（进度 >= 95%）
     */
    @Query("SELECT COUNT(*) FROM reading_progress WHERE progress_percent >= 0.95")
    suspend fun getCompletedBooksCount(): Int
    
    /**
     * 获取正在阅读的图书数量（0% < 进度 < 95%）
     */
    @Query("SELECT COUNT(*) FROM reading_progress WHERE progress_percent > 0 AND progress_percent < 0.95")
    suspend fun getReadingBooksCount(): Int
}

/**
 * 阅读进度统计数据类
 */
data class ProgressStatistics(
    val totalBooksWithProgress: Int,
    val averageProgress: Float,
    val maxProgress: Float,
    val minProgress: Float
) 