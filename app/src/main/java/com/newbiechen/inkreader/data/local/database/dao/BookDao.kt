package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.newbiechen.inkreader.data.local.database.entities.BookEntity

/**
 * 图书数据访问对象
 * 
 * 定义图书相关的数据库操作方法，支持响应式查询和事务处理
 */
@Dao
interface BookDao {
    
    /**
     * 获取所有未删除的图书（响应式）
     * 按最后打开时间降序排列
     */
    @Query("""
        SELECT * FROM books 
        WHERE is_deleted = 0 
        ORDER BY last_opened_at DESC
    """)
    fun getAllBooksFlow(): Flow<List<BookEntity>>
    
    /**
     * 获取所有未删除的图书（一次性查询）
     */
    @Query("""
        SELECT * FROM books 
        WHERE is_deleted = 0 
        ORDER BY last_opened_at DESC
    """)
    suspend fun getAllBooks(): List<BookEntity>
    
    /**
     * 根据ID获取图书
     */
    @Query("SELECT * FROM books WHERE book_id = :bookId AND is_deleted = 0")
    suspend fun getBookById(bookId: String): BookEntity?
    
    /**
     * 根据文件路径获取图书
     */
    @Query("SELECT * FROM books WHERE file_path = :filePath")
    suspend fun getBookByFilePath(filePath: String): BookEntity?
    
    /**
     * 插入图书
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long
    
    /**
     * 批量插入图书
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>): List<Long>
    
    /**
     * 更新图书信息
     */
    @Update
    suspend fun updateBook(book: BookEntity): Int
    
    /**
     * 更新图书最后打开时间
     */
    @Query("""
        UPDATE books 
        SET last_opened_at = :timestamp 
        WHERE book_id = :bookId
    """)
    suspend fun updateLastOpenedTime(bookId: String, timestamp: Long): Int
    
    /**
     * 软删除图书
     */
    @Query("""
        UPDATE books 
        SET is_deleted = 1 
        WHERE book_id = :bookId
    """)
    suspend fun softDeleteBook(bookId: String): Int
    
    /**
     * 物理删除图书
     */
    @Delete
    suspend fun deleteBook(book: BookEntity): Int
    
    /**
     * 搜索图书（按标题和作者）
     */
    @Query("""
        SELECT * FROM books 
        WHERE is_deleted = 0 
        AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%')
        ORDER BY 
            CASE 
                WHEN title LIKE :query || '%' THEN 1
                WHEN author LIKE :query || '%' THEN 2
                ELSE 3
            END,
            last_opened_at DESC
    """)
    suspend fun searchBooks(query: String): List<BookEntity>
    
    /**
     * 获取最近阅读的图书
     */
    @Query("""
        SELECT * FROM books 
        WHERE is_deleted = 0 
        ORDER BY last_opened_at DESC 
        LIMIT :limit
    """)
    suspend fun getRecentBooks(limit: Int): List<BookEntity>
    
    /**
     * 检查文件路径是否已存在
     */
    @Query("SELECT COUNT(*) FROM books WHERE file_path = :filePath")
    suspend fun isFilePathExists(filePath: String): Int
    
    /**
     * 获取图书总数
     */
    @Query("SELECT COUNT(*) FROM books WHERE is_deleted = 0")
    suspend fun getBookCount(): Int
    
    /**
     * 获取图书统计信息
     */
    @Query("""
        SELECT 
            COUNT(*) as total_books,
            SUM(file_size) as total_size,
            AVG(file_size) as avg_size
        FROM books 
        WHERE is_deleted = 0
    """)
    suspend fun getBookStatistics(): BookStatistics?
    
    /**
     * 清理已删除的图书（物理删除）
     */
    @Query("DELETE FROM books WHERE is_deleted = 1")
    suspend fun cleanupDeletedBooks(): Int
}

/**
 * 图书统计信息数据类
 */
data class BookStatistics(
    val totalBooks: Int,
    val totalSize: Long,
    val avgSize: Long
) 