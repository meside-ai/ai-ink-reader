package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import com.newbiechen.inkreader.data.local.database.entities.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 章节数据访问对象
 */
@Dao
interface ChapterDao {
    
    /**
     * 根据图书ID获取所有章节（流式）
     */
    @Query("""
        SELECT * FROM chapters 
        WHERE book_id = :bookId 
        ORDER BY `order` ASC
    """)
    fun getChaptersByBookIdFlow(bookId: String): Flow<List<ChapterEntity>>
    
    /**
     * 根据图书ID获取所有章节
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId ORDER BY `order` ASC")
    suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity>
    
    /**
     * 根据章节ID获取章节
     */
    @Query("SELECT * FROM chapters WHERE chapter_id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?
    
    /**
     * 根据图书ID和顺序获取章节
     */
    @Query("SELECT * FROM chapters WHERE book_id = :bookId AND `order` = :order")
    suspend fun getChapterByOrder(bookId: String, order: Int): ChapterEntity?
    
    /**
     * 获取图书的章节总数
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE book_id = :bookId")
    suspend fun getChapterCount(bookId: String): Int
    
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
     * 更新章节
     */
    @Update
    suspend fun updateChapter(chapter: ChapterEntity): Int
    
    /**
     * 删除章节
     */
    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity): Int
    
    /**
     * 根据图书ID删除所有章节
     */
    @Query("DELETE FROM chapters WHERE book_id = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String): Int
    
    /**
     * 根据章节ID删除章节
     */
    @Query("DELETE FROM chapters WHERE chapter_id = :chapterId")
    suspend fun deleteChapterById(chapterId: String): Int
} 