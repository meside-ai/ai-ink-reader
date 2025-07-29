package com.newbiechen.inkreader.data.repositories

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.data.local.database.dao.BookDao
import com.newbiechen.inkreader.data.local.database.dao.ChapterDao
import com.newbiechen.inkreader.data.local.database.entities.toDomain
import com.newbiechen.inkreader.data.local.database.entities.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图书仓库实现 - Room数据库版本
 */
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao
) : BookRepository {
    
    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooksFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getBookById(bookId: String): Book? {
        return try {
            bookDao.getBookById(bookId)?.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "获取图书失败: $bookId", e)
            null
        }
    }
    
    override suspend fun addBook(book: Book): Result<Book> {
        return try {
            val insertedId = bookDao.insertBook(book.toEntity())
            if (insertedId > 0) {
                Result.success(book)
            } else {
                Result.failure(RuntimeException("保存图书失败"))
            }
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "添加图书失败", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            val deletedRows = bookDao.deleteBookById(bookId)
            if (deletedRows > 0) {
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("删除图书失败，未找到对应记录"))
            }
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "删除图书失败: $bookId", e)
            Result.failure(e)
        }
    }
    
    override suspend fun searchBooks(query: String): List<Book> {
        return try {
            if (query.isBlank()) {
                emptyList()
            } else {
                bookDao.searchBooks(query).map { it.toDomain() }
            }
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "搜索图书失败: $query", e)
            emptyList()
        }
    }
    
    override suspend fun getBookChapters(bookId: String): List<Chapter> {
        return try {
            chapterDao.getChaptersByBookId(bookId).map { it.toDomain() }
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "获取图书章节失败: $bookId", e)
            emptyList()
        }
    }
    
    /**
     * 添加示例数据（用于测试）
     */
    suspend fun addSampleData() {
        try {
            // 检查是否已有数据
            val bookCount = bookDao.getBooksCount()
            if (bookCount == 0) {
                // 添加示例图书 (使用特殊标识表示内存数据)
                val sampleBooks = listOf(
                    Book(
                        bookId = "sample_1",
                        filePath = "internal://sample_data/book1",
                        title = "墨水屏阅读器使用指南",
                        author = "开发团队",
                        publisher = "Ink Reader",
                        totalChapters = 3
                    ),
                    Book(
                        bookId = "sample_2",
                        filePath = "internal://sample_data/book2",
                        title = "Android开发实战",
                        author = "技术专家",
                        publisher = "Tech Press",
                        totalChapters = 5
                    )
                )
                
                sampleBooks.forEach { book ->
                    bookDao.insertBook(book.toEntity())
                    
                    // 为每本书添加示例章节
                    val chapters = (1..book.totalChapters).map { index ->
                        Chapter(
                            chapterId = "${book.bookId}_chapter_$index",
                            bookId = book.bookId,
                            title = "第${index}章",
                            content = generateSampleChapterContent(book.title, index),
                            order = index,
                            wordCount = 800 + index * 200
                        )
                    }
                    chapterDao.insertChapters(chapters.map { it.toEntity() })
                }
                
                android.util.Log.d("BookRepository", "示例数据添加完成")
            }
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "添加示例数据失败", e)
        }
    }
    
    /**
     * Generate sample chapter content
     */
    private fun generateSampleChapterContent(bookTitle: String, chapterIndex: Int): String {
        return when {
            bookTitle.contains("墨水屏阅读器") -> {
                when (chapterIndex) {
                    1 -> """
                        <h1>第一章：墨水屏阅读器介绍</h1>
                        
                        <p>欢迎使用墨水屏阅读器！这是一款专为热爱阅读的用户设计的现代化电子书阅读应用。</p>
                        
                        <h2>主要特性</h2>
                        <ul>
                        <li><strong>舒适阅读体验</strong>：优化的文本排版和多种字体选择</li>
                        <li><strong>智能AI问答</strong>：选中文本即可获得AI智能解答</li>
                        <li><strong>多格式支持</strong>：支持EPUB、PDF等多种电子书格式</li>
                        <li><strong>个性化设置</strong>：自定义字体大小、背景色、行间距等</li>
                        </ul>
                        
                        <p>本应用采用最新的Android技术栈开发，包括Kotlin、Jetpack Compose、Room数据库等，为用户提供流畅稳定的阅读体验。</p>
                        
                        <blockquote>
                        "阅读是人类进步的阶梯，而好的阅读工具能让这个过程更加愉悦。"
                        </blockquote>
                    """.trimIndent()
                    
                    2 -> """
                        <h1>第二章：基本功能使用</h1>
                        
                        <h2>导入电子书</h2>
                        <p>您可以通过以下方式导入电子书到应用中：</p>
                        <ol>
                        <li>点击主界面的"+"按钮</li>
                        <li>选择"从文件导入"</li>
                        <li>浏览并选择您的EPUB文件</li>
                        <li>等待导入完成</li>
                        </ol>
                        
                        <h2>阅读界面操作</h2>
                        <p>在阅读界面中，您可以：</p>
                        <ul>
                        <li><strong>翻页</strong>：左右滑动或点击屏幕边缘</li>
                        <li><strong>调整字体</strong>：双指缩放或在设置中调整</li>
                        <li><strong>选择文本</strong>：长按文本开始选择</li>
                        <li><strong>AI问答</strong>：选中文本后点击AI按钮</li>
                        </ul>
                        
                        <p>这些功能都经过精心设计，确保您在任何环境下都能获得最佳的阅读体验。</p>
                    """.trimIndent()
                    
                    3 -> """
                        <h1>第三章：AI智能问答功能</h1>
                        
                        <p>我们的AI智能问答功能是本应用的核心特色之一，它能够帮助您更好地理解阅读内容。</p>
                        
                        <h2>支持的问答类型</h2>
                        <ul>
                        <li><strong>概念解释</strong>：解释专业术语和复杂概念</li>
                        <li><strong>翻译功能</strong>：中英文双向翻译</li>
                        <li><strong>内容总结</strong>：提取段落要点和核心信息</li>
                        <li><strong>词汇学习</strong>：词汇释义和用法示例</li>
                        <li><strong>内容扩展</strong>：相关知识点的拓展说明</li>
                        </ul>
                        
                        <h2>使用方法</h2>
                        <ol>
                        <li>在阅读时选中您感兴趣的文本</li>
                        <li>系统会自动识别最适合的问答类型</li>
                        <li>点击AI按钮获取智能回答</li>
                        <li>回答会保存到对话历史中供后续查看</li>
                        </ol>
                        
                        <p>AI功能基于先进的自然语言处理技术，能够提供准确、详细的回答，是您学习和理解的得力助手。</p>
                    """.trimIndent()
                    
                    else -> "第${chapterIndex}章内容"
                }
            }
            
            bookTitle.contains("Android开发") -> {
                when (chapterIndex) {
                    1 -> """
                        <h1>第一章：Android开发环境搭建</h1>
                        
                        <p>Android开发是当今移动应用开发的重要方向。本章将引导您搭建完整的Android开发环境。</p>
                        
                        <h2>必需工具</h2>
                        <ul>
                        <li><strong>Android Studio</strong>：官方IDE，功能强大</li>
                        <li><strong>Android SDK</strong>：软件开发工具包</li>
                        <li><strong>Java/Kotlin</strong>：主要编程语言</li>
                        <li><strong>Gradle</strong>：构建工具</li>
                        </ul>
                        
                        <h2>环境配置步骤</h2>
                        <ol>
                        <li>下载并安装Android Studio</li>
                        <li>配置SDK路径和版本</li>
                        <li>创建Android虚拟设备(AVD)</li>
                        <li>运行Hello World项目</li>
                        </ol>
                        
                        <p>正确的开发环境是成功开发Android应用的基础，请确保每一步都正确完成。</p>
                    """.trimIndent()
                    
                    2 -> """
                        <h1>第二章：Kotlin语言基础</h1>
                        
                        <p>Kotlin是Google官方推荐的Android开发语言，具有简洁、安全、互操作性强等特点。</p>
                        
                        <h2>Kotlin优势</h2>
                        <ul>
                        <li><strong>简洁性</strong>：减少样板代码</li>
                        <li><strong>安全性</strong>：空安全和类型安全</li>
                        <li><strong>互操作性</strong>：与Java完全兼容</li>
                        <li><strong>函数式编程</strong>：支持高阶函数和Lambda</li>
                        </ul>
                        
                        <h2>基本语法</h2>
                        <pre><code>
                        // 变量声明
                        val name: String = "Android Developer"
                        var age: Int = 25
                        
                        // 函数定义
                        fun greet(userName: String): String {
                            return "Hello, " + userName + "!"
                        }
                        
                        // 数据类
                        data class User(val id: Int, val name: String)
                        </code></pre>
                        
                        <p>掌握Kotlin语法是Android开发的重要基础，建议多加练习。</p>
                    """.trimIndent()
                    
                    else -> """
                        <h1>第${chapterIndex}章：Android开发进阶</h1>
                        
                        <p>本章将深入探讨Android开发的高级技术和最佳实践。</p>
                        
                        <h2>核心概念</h2>
                        <ul>
                        <li>Activity生命周期管理</li>
                        <li>Fragment的使用和通信</li>
                        <li>数据绑定和视图绑定</li>
                        <li>网络请求和数据存储</li>
                        </ul>
                        
                        <p>通过学习这些概念，您将能够开发出功能完整、性能优良的Android应用。</p>
                        
                        <h2>实践建议</h2>
                        <p>理论学习要结合实际项目练习，建议您创建一个实际的应用项目来巩固所学知识。</p>
                    """.trimIndent()
                }
            }
            
            else -> """
                <h1>第${chapterIndex}章</h1>
                <p>这是《${bookTitle}》第${chapterIndex}章的示例内容。</p>
                <p>本章包含了丰富的文本内容，用于演示阅读功能和AI问答功能。</p>
            """.trimIndent()
        }
    }
} 