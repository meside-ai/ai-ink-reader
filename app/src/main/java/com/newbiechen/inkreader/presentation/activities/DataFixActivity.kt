package com.newbiechen.inkreader.presentation.activities

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.data.repositories.BookRepositoryImpl
import com.newbiechen.inkreader.data.local.database.dao.ChapterDao
import com.newbiechen.inkreader.data.local.database.entities.toEntity
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DataFixActivity : AppCompatActivity() {
    
    @Inject
    lateinit var bookRepository: BookRepositoryImpl
    
    @Inject
    lateinit var chapterDao: ChapterDao
    
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createUI()
        checkData()
    }
    
    private fun createUI() {
        val scrollView = ScrollView(this)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }
        
        // 标题
        val titleText = TextView(this).apply {
            text = "数据修复工具 🔧"
            textSize = 24f
            setTextColor(getColor(R.color.text_primary))
        }
        
        // 状态文本
        statusText = TextView(this).apply {
            text = "正在检查数据..."
            textSize = 16f
            setTextColor(getColor(R.color.text_primary))
            setPadding(0, 24, 0, 24)
        }
        
        // 修复按钮
        val fixButton = Button(this).apply {
            text = "修复数据库"
            textSize = 16f
            setPadding(32, 16, 32, 16)
            setOnClickListener {
                fixData()
            }
        }
        
        // 日志文本
        logText = TextView(this).apply {
            text = ""
            textSize = 12f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, 24, 0, 0)
            setTypeface(android.graphics.Typeface.MONOSPACE)
        }
        
        mainLayout.addView(titleText)
        mainLayout.addView(statusText)
        mainLayout.addView(fixButton)
        mainLayout.addView(logText)
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun checkData() {
        lifecycleScope.launch {
            try {
                val books = bookRepository.getAllBooks()
                books.collect { bookList ->
                    runOnUiThread {
                        statusText.text = "数据库状态：找到 ${bookList.size} 本图书"
                        appendLog("✅ 数据库连接正常")
                        appendLog("📚 图书数量：${bookList.size}")
                        
                        bookList.forEach { book ->
                            appendLog("- ${book.title} (${book.author})")
                        }
                        
                        if (bookList.isEmpty()) {
                            appendLog("⚠️ 没有找到任何图书数据")
                            appendLog("💡 点击「修复数据库」按钮添加示例数据")
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "数据库错误：${e.message}"
                    appendLog("❌ 数据库连接失败：$e")
                }
            }
        }
    }
    
    private fun fixData() {
        lifecycleScope.launch {
            try {
                appendLog("\n🔧 开始修复数据...")
                
                // 手动添加示例数据
                val sampleBooks = listOf(
                    Book(
                        bookId = "sample_1",
                        filePath = "/sample/book1.epub",
                        title = "墨水屏阅读器使用指南",
                        author = "开发团队",
                        publisher = "Ink Reader",
                        totalChapters = 3
                    ),
                    Book(
                        bookId = "sample_2", 
                        filePath = "/sample/book2.epub",
                        title = "Android开发实战",
                        author = "技术专家",
                        publisher = "Tech Press",
                        totalChapters = 5
                    )
                )
                
                sampleBooks.forEach { book ->
                    val result = bookRepository.addBook(book)
                    if (result.isSuccess) {
                        appendLog("✅ 添加图书：${book.title}")
                        
                        // 添加章节
                        val chapters = (1..book.totalChapters).map { index ->
                            Chapter(
                                chapterId = "${book.bookId}_chapter_$index",
                                bookId = book.bookId,
                                title = "第${index}章",
                                content = "这是《${book.title}》第${index}章的内容...\n\n" +
                                        "本章节包含了详细的说明和示例。\n" +
                                        "这里是更多的内容，用于测试章节显示功能。\n" +
                                        "章节内容可以很长，支持滚动阅读。",
                                order = index,
                                wordCount = 500 + index * 100
                            )
                        }
                        
                        try {
                            chapterDao.insertChapters(chapters.map { it.toEntity() })
                            appendLog("  ↳ 添加 ${chapters.size} 个章节")
                        } catch (e: Exception) {
                            appendLog("  ↳ ❌ 章节添加失败: $e")
                        }
                    } else {
                        appendLog("❌ 图书添加失败：${book.title}")
                    }
                }
                
                appendLog("🎉 数据修复完成！")
                runOnUiThread {
                    statusText.text = "修复完成，请返回主界面查看"
                }
                
            } catch (e: Exception) {
                appendLog("❌ 修复过程出错：$e")
                runOnUiThread {
                    statusText.text = "修复失败：${e.message}"
                }
            }
        }
    }
    
    private fun appendLog(message: String) {
        runOnUiThread {
            logText.text = "${logText.text}\n$message"
        }
    }
} 