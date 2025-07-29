package com.newbiechen.inkreader.presentation.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.data.repositories.BookRepositoryImpl
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.presentation.adapters.BookListAdapter
import com.newbiechen.inkreader.presentation.viewmodels.BookListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主活动 - 墨水屏阅读器
 * 显示图书列表和基本功能
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private val viewModel: BookListViewModel by viewModels()
    
    @Inject
    lateinit var bookRepository: BookRepositoryImpl
    
    private lateinit var titleText: TextView
    private lateinit var statusText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var importBookButton: Button
    private lateinit var refreshButton: Button
    
    private lateinit var bookAdapter: BookListAdapter
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bookId = FilePickerActivity.getBookIdFromResult(result.data)
            if (bookId != null) {
                Toast.makeText(this, "图书导入成功！", Toast.LENGTH_SHORT).show()
                viewModel.refresh() // Refresh book list
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createUI()
        setupRecyclerView()
        observeViewModel()
        initializeData()
    }
    
    private fun createUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 48)
        }
        
        // 标题
        titleText = TextView(this).apply {
            text = "墨水屏阅读器 📚"
            textSize = 24f
            setTextColor(getColor(R.color.text_primary))
            gravity = android.view.Gravity.CENTER
        }
        
        // 状态文本
        statusText = TextView(this).apply {
            text = "正在加载图书..."
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        
        // 按钮容器
        buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }
        
        // 导入图书按钮（原添加示例图书按钮）
        importBookButton = Button(this).apply {
            text = "导入EPUB图书"
            setOnClickListener {
                openFilePicker()
            }
        }
        
        // 刷新按钮
        refreshButton = Button(this).apply {
            text = "刷新列表"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 32 // 8dp margin
            }
            setOnClickListener {
                viewModel.refresh()
            }
        }
        
        buttonContainer.addView(importBookButton)
        buttonContainer.addView(refreshButton)
        
        // RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // 占用剩余空间
            )
        }
        
        mainLayout.addView(titleText)
        mainLayout.addView(statusText)
        mainLayout.addView(buttonContainer)
        mainLayout.addView(recyclerView)
        
        setContentView(mainLayout)
    }
    
    private fun setupRecyclerView() {
        bookAdapter = BookListAdapter(
            onBookClick = { book ->
                // 跳转到图书详情页面
                val intent = BookDetailActivity.createIntent(this, book.bookId)
                startActivity(intent)
            },
            onBookLongClick = { book ->
                // 长按显示操作菜单
                showBookOptions(book)
                true
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bookAdapter
        }
    }
    
    private fun observeViewModel() {
        // 观察图书列表
        lifecycleScope.launch {
            viewModel.books.collectLatest { books ->
                bookAdapter.submitList(books)
                updateStatus(books)
            }
        }
        
        // 观察加载状态
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                statusText.text = if (isLoading) "正在加载..." else ""
            }
        }
        
        // 观察错误信息
        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }
    
    private fun updateStatus(books: List<Book>) {
        statusText.text = when {
            books.isEmpty() -> "暂无图书，点击添加示例图书"
            else -> "共 ${books.size} 本图书"
        }
    }
    
    private fun showBookOptions(book: Book) {
        // 显示图书操作选项
        val options = arrayOf(
            "查看详情",
            "删除图书"
        )
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(book.title)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // 查看详情
                    val intent = BookDetailActivity.createIntent(this, book.bookId)
                    startActivity(intent)
                }
                1 -> {
                    // 删除图书
                    confirmDeleteBook(book)
                }
            }
        }
        builder.show()
    }
    
    private fun confirmDeleteBook(book: Book) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this) 
        builder.setTitle("删除图书")
        builder.setMessage("确定要删除《${book.title}》吗？此操作不可撤销。")
        builder.setPositiveButton("删除") { _, _ ->
            viewModel.toggleBookSelection(book.bookId)
            viewModel.deleteSelectedBooks()
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }
    
    private fun initializeData() {
        // 强制初始化示例数据
        lifecycleScope.launch {
            try {
                android.util.Log.d("MainActivity", "开始初始化示例数据...")
                
                // 检查数据库中是否有数据
                val books = bookRepository.searchBooks("") // 获取所有图书
                android.util.Log.d("MainActivity", "当前数据库中有 ${books.size} 本书")
                
                if (books.isEmpty()) {
                    android.util.Log.d("MainActivity", "数据库为空，添加示例数据...")
                    bookRepository.addSampleData()
                    android.util.Log.d("MainActivity", "示例数据添加完成")
                } else {
                    android.util.Log.d("MainActivity", "数据库已有数据，跳过初始化")
                }
                
                // 刷新UI
                runOnUiThread {
                    viewModel.refresh()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "初始化数据失败", e)
                runOnUiThread {
                    statusText.text = "数据初始化失败：${e.message}"
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = FilePickerActivity.createIntent(this)
        filePickerLauncher.launch(intent)
    }
} 