package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import com.newbiechen.inkreader.data.local.database.entities.ChapterEntity

/**
 * 章节数据访问对象
 * 
 * 定义章节相关的数据库操作方法
 */
@Dao
interface ChapterDao {
    
    /**
     * 根据图书ID获取所有章节
     */
    @Query("""
        SELECT * FROM chapters 
        WHERE book_id = :bookId 
        ORDER BY chapter_index ASC
    """)
    suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity>
    
    /**
     * 根据章节ID获取章节
     */
    @Query("SELECT * FROM chapters WHERE chapter_id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?
    
    /**
     * 根据图书ID和章节索引获取章节
     */
    @Query("""
        SELECT * FROM chapters 
        WHERE book_id = :bookId AND chapter_index = :chapterIndex
    """)
    suspend fun getChapterByIndex(bookId: String, chapterIndex: Int): ChapterEntity?
    
    /**
     * 获取下一章节
     */
    @Query("""
        SELECT * FROM chapters 
        WHERE book_id = :bookId AND chapter_index > :currentIndex 
        ORDER BY chapter_index ASC 
        LIMIT 1
    """)
    suspend fun getNextChapter(bookId: String, currentIndex: Int): ChapterEntity?
    
    /**
     * 获取上一章节
     */
    @Query("""
        SELECT * FROM chapters 
        WHERE book_id = :bookId AND chapter_index < :currentIndex 
        ORDER BY chapter_index DESC 
        LIMIT 1
    """)
    suspend fun getPreviousChapter(bookId: String, currentIndex: Int): ChapterEntity?
    
    /**
     * 插入章节
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long
    
    /**
     * 批量插入章节
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>): List<Long>
    
    /**
     * 更新章节信息
     */
    @Update
    suspend fun updateChapter(chapter: ChapterEntity): Int
    
    /**
     * 删除章节
     */
    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity): Int
    
    /**
     * 删除图书的所有章节
     */
    @Query("DELETE FROM chapters WHERE book_id = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String): Int
    
    /**
     * 获取图书的章节总数
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE book_id = :bookId")
    suspend fun getChapterCount(bookId: String): Int
    
    /**
     * 获取图书的总字数
     */
    @Query("SELECT SUM(word_count) FROM chapters WHERE book_id = :bookId")
    suspend fun getTotalWordCount(bookId: String): Int?
    
    /**
     * 获取图书的预估总阅读时间
     */
    @Query("SELECT SUM(estimated_reading_time) FROM chapters WHERE book_id = :bookId")
    suspend fun getTotalEstimatedReadingTime(bookId: String): Int?
    
    /**
     * 更新章节字数和阅读时间
     */
    @Query("""
        UPDATE chapters 
        SET word_count = :wordCount, estimated_reading_time = :readingTime 
        WHERE chapter_id = :chapterId
    """)
    suspend fun updateChapterStats(chapterId: String, wordCount: Int, readingTime: Int): Int
    
    /**
     * 事务：删除图书时同时删除章节
     */
    @Transaction
    suspend fun deleteBookChapters(bookId: String) = deleteChaptersByBookId(bookId)
} 