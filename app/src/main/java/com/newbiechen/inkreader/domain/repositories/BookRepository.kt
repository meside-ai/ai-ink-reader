package com.newbiechen.inkreader.domain.repositories

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * 图书仓储接口
 * 
 * 定义图书相关的数据访问操作
 */
interface BookRepository {
    
    /**
     * 获取所有图书列表（以Flow形式，支持实时更新）
     */
    fun getAllBooks(): Flow<List<Book>>
    
    /**
     * 根据ID获取图书信息
     */
    suspend fun getBookById(bookId: String): Book?
    
    /**
     * 添加新图书（包含EPUB解析）
     */
    suspend fun addBook(filePath: String): Result<Book>
    
    /**
     * 更新图书信息
     */
    suspend fun updateBook(book: Book): Result<Unit>
    
    /**
     * 删除图书（软删除）
     */
    suspend fun deleteBook(bookId: String): Result<Unit>
    
    /**
     * 获取图书的所有章节
     */
    suspend fun getBookChapters(bookId: String): List<Chapter>
    
    /**
     * 获取指定章节信息
     */
    suspend fun getChapterById(chapterId: String): Chapter?
    
    /**
     * 获取章节内容
     */
    suspend fun getChapterContent(bookId: String, chapterIndex: Int): Result<String>
    
    /**
     * 更新图书最后打开时间
     */
    suspend fun updateLastOpenedTime(bookId: String): Result<Unit>
    
    /**
     * 搜索图书（按标题、作者）
     */
    suspend fun searchBooks(query: String): List<Book>
    
    /**
     * 检查EPUB文件是否已导入
     */
    suspend fun isBookExists(filePath: String): Boolean
} 