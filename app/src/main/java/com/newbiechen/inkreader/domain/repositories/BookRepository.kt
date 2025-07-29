package com.newbiechen.inkreader.domain.repositories

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import kotlinx.coroutines.flow.Flow

/**
 * 图书仓库接口
 * 定义图书相关的业务操作
 */
interface BookRepository {
    
    /**
     * 获取所有图书
     */
    fun getAllBooks(): Flow<List<Book>>
    
    /**
     * 根据ID获取图书
     */
    suspend fun getBookById(bookId: String): Book?
    
    /**
     * 添加图书
     */
    suspend fun addBook(book: Book): Result<Book>
    
    /**
     * 删除图书
     */
    suspend fun deleteBook(bookId: String): Result<Unit>
    
    /**
     * 搜索图书
     */
    suspend fun searchBooks(query: String): List<Book>
    
    /**
     * 获取图书章节
     */
    suspend fun getBookChapters(bookId: String): List<Chapter>
} 