package com.newbiechen.inkreader.data.local.database.dao

import androidx.room.*
import com.newbiechen.inkreader.data.local.database.entities.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * 图书数据访问对象
 */
@Dao
interface BookDao {
    
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
     * 更新图书
     */
    @Update
    suspend fun updateBook(book: BookEntity): Int
    
    /**
     * 根据ID删除图书（软删除）
     */
    @Query("UPDATE books SET is_deleted = 1 WHERE book_id = :bookId")
    suspend fun deleteBookById(bookId: String): Int
    
    /**
     * 根据ID获取图书
     */
    @Query("SELECT * FROM books WHERE book_id = :bookId AND is_deleted = 0")
    suspend fun getBookById(bookId: String): BookEntity?
    
    /**
     * 获取所有图书（流式）
     */
    @Query("SELECT * FROM books WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getAllBooksFlow(): Flow<List<BookEntity>>
    
    /**
     * 获取所有图书（一次性）
     */
    @Query("SELECT * FROM books WHERE is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getAllBooks(): List<BookEntity>
    
    /**
     * 根据标题搜索图书
     */
    @Query("""
        SELECT * FROM books 
        WHERE is_deleted = 0 
        AND (title LIKE '%' || :query || '%' 
             OR author LIKE '%' || :query || '%'
             OR file_path LIKE '%' || :query || '%')
        ORDER BY 
            CASE 
                WHEN title LIKE '%' || :query || '%' THEN 1
                WHEN author LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END,
            created_at DESC
    """)
    suspend fun searchBooks(query: String): List<BookEntity>
    
    /**
     * 根据作者获取图书
     */
    @Query("SELECT * FROM books WHERE author = :author AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getBooksByAuthor(author: String): List<BookEntity>
    
    /**
     * 根据文件路径获取图书
     */
    @Query("SELECT * FROM books WHERE file_path = :filePath AND is_deleted = 0")
    suspend fun getBookByFilePath(filePath: String): BookEntity?
    
    /**
     * 检查图书是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM books WHERE file_path = :filePath AND is_deleted = 0")
    suspend fun bookExists(filePath: String): Boolean
    
    /**
     * 获取图书总数
     */
    @Query("SELECT COUNT(*) FROM books WHERE is_deleted = 0")
    suspend fun getBooksCount(): Int
    
    /**
     * 获取最近添加的图书
     */
    @Query("SELECT * FROM books WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentBooks(limit: Int = 10): List<BookEntity>
    
    /**
     * 获取最近阅读的图书
     */
    @Query("SELECT * FROM books WHERE is_deleted = 0 ORDER BY last_opened_at DESC LIMIT :limit")
    suspend fun getRecentlyReadBooks(limit: Int = 10): List<BookEntity>
    
    /**
     * 更新图书最后打开时间
     */
    @Query("UPDATE books SET last_opened_at = :lastOpenedAt WHERE book_id = :bookId")
    suspend fun updateLastOpenedTime(bookId: String, lastOpenedAt: Long = System.currentTimeMillis()): Int
    
    /**
     * 清理已删除的图书（物理删除）
     */
    @Query("DELETE FROM books WHERE is_deleted = 1")
    suspend fun cleanupDeletedBooks(): Int
} 