package com.newbiechen.inkreader.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.data.local.database.dao.BookDao
import com.newbiechen.inkreader.data.local.database.dao.ChapterDao
import com.newbiechen.inkreader.data.local.database.entities.toDomain
import com.newbiechen.inkreader.data.local.database.entities.toEntity
import com.newbiechen.inkreader.core.epub.EpubParserService
import com.newbiechen.inkreader.core.epub.models.EpubParseResult

/**
 * 图书仓储接口实现
 * 
 * 整合数据库操作和EPUB解析服务，提供完整的图书管理功能
 */
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val epubParserService: EpubParserService
) : BookRepository {
    
    /**
     * 获取所有图书列表（响应式）
     */
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooksFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * 根据ID获取图书信息
     */
    override suspend fun getBookById(bookId: String): Book? {
        return try {
            bookDao.getBookById(bookId)?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "获取图书失败: $bookId")
            null
        }
    }
    
    /**
     * 添加新图书（包含EPUB解析）
     */
    override suspend fun addBook(filePath: String): Result<Book> {
        return try {
            Timber.d("开始添加图书: $filePath")
            
            // 检查文件是否已经导入
            if (isBookExists(filePath)) {
                return Result.failure(IllegalArgumentException("图书已存在，请勿重复导入"))
            }
            
            // 验证EPUB文件
            val validationResult = epubParserService.validateEpubFile(filePath)
            if (!validationResult.isValid) {
                val errorMessage = "EPUB文件无效: ${validationResult.errors.joinToString(", ")}"
                return Result.failure(IllegalArgumentException(errorMessage))
            }
            
            // 解析EPUB文件
            when (val parseResult = epubParserService.parseEpub(filePath)) {
                is EpubParseResult.Success -> {
                    val metadata = parseResult.metadata
                    
                    // 验证解析结果
                    if (!metadata.isValid()) {
                        return Result.failure(IllegalArgumentException("EPUB解析结果无效"))
                    }
                    
                    // 转换为Domain实体
                    val book = metadata.toBook()
                    val chapters = metadata.getChapters()
                    
                    // 保存到数据库（使用事务）
                    val insertedBookId = bookDao.insertBook(book.toEntity())
                    if (insertedBookId <= 0) {
                        return Result.failure(RuntimeException("保存图书信息失败"))
                    }
                    
                    // 保存章节信息
                    if (chapters.isNotEmpty()) {
                        val chapterEntities = chapters.map { it.toEntity() }
                        val insertedChapterIds = chapterDao.insertChapters(chapterEntities)
                        
                        if (insertedChapterIds.size != chapters.size) {
                            Timber.w("部分章节保存失败: ${chapters.size} -> ${insertedChapterIds.size}")
                        }
                    }
                    
                    Timber.i("图书添加成功: ${book.title} (${chapters.size}章)")
                    Result.success(book)
                }
                
                is EpubParseResult.Error -> {
                    Timber.e(parseResult.exception, "EPUB解析失败: ${parseResult.message}")
                    Result.failure(parseResult.exception)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "添加图书异常: $filePath")
            Result.failure(e)
        }
    }
    
    /**
     * 更新图书信息
     */
    override suspend fun updateBook(book: Book): Result<Unit> {
        return try {
            val updatedRows = bookDao.updateBook(book.toEntity())
            if (updatedRows > 0) {
                Timber.d("图书更新成功: ${book.title}")
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("图书更新失败，未找到对应记录"))
            }
        } catch (e: Exception) {
            Timber.e(e, "更新图书失败: ${book.bookId}")
            Result.failure(e)
        }
    }
    
    /**
     * 删除图书（软删除）
     */
    override suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            val deletedRows = bookDao.softDeleteBook(bookId)
            if (deletedRows > 0) {
                Timber.d("图书删除成功: $bookId")
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("图书删除失败，未找到对应记录"))
            }
        } catch (e: Exception) {
            Timber.e(e, "删除图书失败: $bookId")
            Result.failure(e)
        }
    }
    
    /**
     * 获取图书的所有章节
     */
    override suspend fun getBookChapters(bookId: String): List<Chapter> {
        return try {
            chapterDao.getChaptersByBookId(bookId).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "获取图书章节失败: $bookId")
            emptyList()
        }
    }
    
    /**
     * 获取指定章节信息
     */
    override suspend fun getChapterById(chapterId: String): Chapter? {
        return try {
            chapterDao.getChapterById(chapterId)?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "获取章节信息失败: $chapterId")
            null
        }
    }
    
    /**
     * 获取章节内容
     */
    override suspend fun getChapterContent(bookId: String, chapterIndex: Int): Result<String> {
        return try {
            // 获取图书信息
            val book = getBookById(bookId)
                ?: return Result.failure(IllegalArgumentException("图书不存在: $bookId"))
            
            // 获取章节信息
            val chapter = chapterDao.getChapterByIndex(bookId, chapterIndex)?.toDomain()
                ?: return Result.failure(IllegalArgumentException("章节不存在: $chapterIndex"))
            
            // 从EPUB文件中读取章节内容
            val content = epubParserService.getChapterContent(book.filePath, chapter.resourcePath)
            
            Timber.d("章节内容获取成功: ${chapter.title} (${content.length} 字符)")
            Result.success(content)
            
        } catch (e: Exception) {
            Timber.e(e, "获取章节内容失败: $bookId, 章节 $chapterIndex")
            Result.failure(e)
        }
    }
    
    /**
     * 更新图书最后打开时间
     */
    override suspend fun updateLastOpenedTime(bookId: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val updatedRows = bookDao.updateLastOpenedTime(bookId, timestamp)
            
            if (updatedRows > 0) {
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("更新最后打开时间失败"))
            }
        } catch (e: Exception) {
            Timber.e(e, "更新最后打开时间失败: $bookId")
            Result.failure(e)
        }
    }
    
    /**
     * 搜索图书（按标题、作者）
     */
    override suspend fun searchBooks(query: String): List<Book> {
        return try {
            if (query.isBlank()) {
                return emptyList()
            }
            
            bookDao.searchBooks(query).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "搜索图书失败: $query")
            emptyList()
        }
    }
    
    /**
     * 检查EPUB文件是否已导入
     */
    override suspend fun isBookExists(filePath: String): Boolean {
        return try {
            bookDao.isFilePathExists(filePath) > 0
        } catch (e: Exception) {
            Timber.e(e, "检查图书是否存在失败: $filePath")
            false
        }
    }
    
    /**
     * 获取最近阅读的图书
     */
    suspend fun getRecentBooks(limit: Int = 10): List<Book> {
        return try {
            bookDao.getRecentBooks(limit).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "获取最近阅读图书失败")
            emptyList()
        }
    }
    
    /**
     * 获取图书统计信息
     */
    suspend fun getBookStatistics(): BookStatistics? {
        return try {
            val stats = bookDao.getBookStatistics()
            stats?.let {
                BookStatistics(
                    totalBooks = it.totalBooks,
                    totalSize = it.totalSize,
                    averageSize = it.avgSize
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "获取图书统计失败")
            null
        }
    }
    
    /**
     * 清理已删除的图书
     */
    suspend fun cleanupDeletedBooks(): Result<Int> {
        return try {
            val cleanedCount = bookDao.cleanupDeletedBooks()
            Timber.d("清理已删除图书: $cleanedCount 本")
            Result.success(cleanedCount)
        } catch (e: Exception) {
            Timber.e(e, "清理已删除图书失败")
            Result.failure(e)
        }
    }
}

/**
 * 图书统计信息（Domain层）
 */
data class BookStatistics(
    val totalBooks: Int,
    val totalSize: Long,
    val averageSize: Long
) {
    
    /**
     * 获取总大小的可读格式
     */
    fun getFormattedTotalSize(): String {
        return formatFileSize(totalSize)
    }
    
    /**
     * 获取平均大小的可读格式
     */
    fun getFormattedAverageSize(): String {
        return formatFileSize(averageSize)
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
            else -> "${bytes / (1024 * 1024 * 1024)}GB"
        }
    }
} 