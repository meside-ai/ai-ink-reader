package com.newbiechen.inkreader.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.presentation.adapters.ChapterListAdapter
import com.newbiechen.inkreader.presentation.viewmodels.BookDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 图书详情Activity
 * 显示图书详细信息和章节列表
 */
@AndroidEntryPoint
class BookDetailActivity : AppCompatActivity() {
    
    companion object {
        private const val EXTRA_BOOK_ID = "book_id"
        
        fun createIntent(context: Context, bookId: String): Intent {
            return Intent(context, BookDetailActivity::class.java).apply {
                putExtra(EXTRA_BOOK_ID, bookId)
            }
        }
    }
    
    private val viewModel: BookDetailViewModel by viewModels()
    
    private lateinit var bookId: String
    private lateinit var scrollView: ScrollView
    private lateinit var titleText: TextView
    private lateinit var authorText: TextView
    private lateinit var publisherText: TextView
    private lateinit var infoText: TextView
    private lateinit var chaptersHeaderText: TextView
    private lateinit var chaptersRecyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var readButton: Button
    
    private lateinit var chapterAdapter: ChapterListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bookId = intent.getStringExtra(EXTRA_BOOK_ID) ?: run {
            Toast.makeText(this, "图书ID不能为空", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        createUI()
        setupChaptersList()
        observeViewModel()
        
        // 加载图书详情
        viewModel.loadBookDetail(bookId)
    }
    
    private fun createUI() {
        // 创建主布局
        scrollView = ScrollView(this).apply {
            setPadding(32, 32, 32, 32)
        }
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // 返回按钮
        backButton = Button(this).apply {
            text = "← 返回"
            setOnClickListener { finish() }
        }
        
        // 图书标题
        titleText = TextView(this).apply {
            text = "加载中..."
            textSize = 24f
            setTextColor(getColor(R.color.text_primary))
            setPadding(0, 24, 0, 8)
        }
        
        // 作者
        authorText = TextView(this).apply {
            text = "作者：加载中..."
            textSize = 16f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, 0, 0, 8)
        }
        
        // 出版社
        publisherText = TextView(this).apply {
            text = "出版社：加载中..."
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(0, 0, 0, 8)
        }
        
        // 图书信息
        infoText = TextView(this).apply {
            text = "图书信息加载中..."
            textSize = 14f
            setTextColor(getColor(R.color.text_hint))
            setPadding(0, 0, 0, 16)
        }
        
        // 开始阅读按钮
        readButton = Button(this).apply {
            text = "开始阅读"
            textSize = 18f
            setPadding(32, 16, 32, 16)
            setOnClickListener {
                // 跳转到阅读界面
                val intent = ReadingActivity.createIntent(this@BookDetailActivity, bookId)
                startActivity(intent)
            }
        }
        
        // 章节标题
        chaptersHeaderText = TextView(this).apply {
            text = "章节列表"
            textSize = 20f
            setTextColor(getColor(R.color.text_primary))
            setPadding(0, 24, 0, 16)
        }
        
        // 章节列表
        chaptersRecyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // 添加所有视图到主布局
        mainLayout.addView(backButton)
        mainLayout.addView(titleText)
        mainLayout.addView(authorText)
        mainLayout.addView(publisherText)
        mainLayout.addView(infoText)
        mainLayout.addView(readButton)
        mainLayout.addView(chaptersHeaderText)
        mainLayout.addView(chaptersRecyclerView)
        
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun setupChaptersList() {
        chapterAdapter = ChapterListAdapter { chapter ->
            // 点击章节开始阅读该章节
            Toast.makeText(this, "开始阅读: ${chapter.title}", Toast.LENGTH_SHORT).show()
            // TODO: 实现章节阅读功能
        }
        
        chaptersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookDetailActivity)
            adapter = chapterAdapter
        }
    }
    
    private fun observeViewModel() {
        // 观察图书详情
        lifecycleScope.launch {
            viewModel.book.collectLatest { book ->
                book?.let { updateBookInfo(it) }
            }
        }
        
        // 观察章节列表
        lifecycleScope.launch {
            viewModel.chapters.collectLatest { chapters ->
                chapterAdapter.submitList(chapters)
                updateChaptersHeader(chapters.size)
            }
        }
        
        // 观察加载状态
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                if (!isLoading) {
                    // 加载完成后更新UI状态
                }
            }
        }
        
        // 观察错误信息
        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(this@BookDetailActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }
    
    private fun updateBookInfo(book: Book) {
        titleText.text = book.title
        authorText.text = "作者：${book.author}"
        publisherText.text = "出版社：${book.publisher ?: "未知"}"
        
        infoText.text = """
            📄 总章节：${book.totalChapters} 章
            🌐 语言：${book.language}
            📁 文件路径：${book.filePath}
            📅 添加时间：${formatTimestamp(book.createdAt)}
            🔖 最后阅读：${formatTimestamp(book.lastOpenedAt)}
        """.trimIndent()
    }
    
    private fun updateChaptersHeader(chapterCount: Int) {
        chaptersHeaderText.text = "章节列表 ($chapterCount)"
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "刚刚"
            diff < 3600_000 -> "${diff / 60_000} 分钟前"
            diff < 86400_000 -> "${diff / 3600_000} 小时前"
            diff < 2592000_000 -> "${diff / 86400_000} 天前"
            else -> {
                val date = java.util.Date(timestamp)
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
            }
        }
    }
} 