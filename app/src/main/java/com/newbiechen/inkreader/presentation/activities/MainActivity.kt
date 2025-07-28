package com.newbiechen.inkreader.presentation.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.databinding.ActivityMainBinding
import com.newbiechen.inkreader.presentation.fragments.BookListFragment
import com.newbiechen.inkreader.presentation.viewmodels.MainViewModel
import com.newbiechen.inkreader.core.files.FileImportResult
import com.newbiechen.inkreader.utils.showToast

/**
 * 主Activity
 * 
 * 应用的入口点，负责：
 * - Fragment导航管理
 * - 文件导入处理
 * - Intent处理（从外部打开EPUB文件）
 * - 全局UI状态管理
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    // 文件选择器
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFileImport(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置视图绑定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        
        // 初始化Fragment
        if (savedInstanceState == null) {
            showBookListFragment()
        }
        
        // 设置UI监听器
        setupUI()
        
        // 观察ViewModel
        observeViewModel()
        
        // 处理Intent（从外部打开文件）
        handleIntent(intent)
        
        Timber.d("MainActivity创建完成")
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    /**
     * 设置UI监听器
     */
    private fun setupUI() {
        // FAB点击事件
        binding.fabAddBook.setOnClickListener {
            openFilePicker()
        }
    }
    
    /**
     * 观察ViewModel状态
     */
    private fun observeViewModel() {
        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            // 可以在这里显示/隐藏全局进度条
            Timber.d("加载状态: $isLoading")
        }
        
        // 观察错误信息
        viewModel.error.observe(this) { error ->
            error?.let {
                showToast(it, Toast.LENGTH_LONG)
                viewModel.clearError()
            }
        }
        
        // 观察成功消息
        viewModel.successMessage.observe(this) { message ->
            message?.let {
                showToast(it)
                viewModel.clearSuccessMessage()
            }
        }
    }
    
    /**
     * 处理Intent
     */
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                // 从外部打开文件
                intent.data?.let { uri ->
                    Timber.d("从外部打开文件: $uri")
                    handleFileImport(uri)
                }
            }
            Intent.ACTION_SEND -> {
                // 分享接收
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                    Timber.d("接收分享文件: $uri")
                    handleFileImport(uri)
                }
            }
        }
    }
    
    /**
     * 打开文件选择器
     */
    private fun openFilePicker() {
        try {
            val intent = viewModel.createFilePickerIntent()
            filePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Timber.e(e, "打开文件选择器失败")
            showToast("打开文件选择器失败")
        }
    }
    
    /**
     * 处理文件导入
     */
    private fun handleFileImport(uri: Uri) {
        lifecycleScope.launch {
            Timber.d("开始处理文件导入: $uri")
            
            when (val result = viewModel.importFile(uri)) {
                is FileImportResult.Success -> {
                    Timber.i("文件导入成功: ${result.fileInfo.fileName}")
                    showToast("《${result.fileInfo.fileName}》导入成功！")
                    
                    // 刷新图书列表
                    getCurrentBookListFragment()?.refreshBooks()
                }
                
                is FileImportResult.Error -> {
                    Timber.e("文件导入失败: ${result.message}")
                    showToast("导入失败：${result.message}")
                }
            }
        }
    }
    
    /**
     * 显示图书列表Fragment
     */
    private fun showBookListFragment() {
        val fragment = BookListFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, BookListFragment.TAG)
            .commit()
    }
    
    /**
     * 获取当前的BookListFragment
     */
    private fun getCurrentBookListFragment(): BookListFragment? {
        return supportFragmentManager.findFragmentByTag(BookListFragment.TAG) as? BookListFragment
    }
    
    /**
     * 显示阅读界面
     */
    fun showReadingActivity(bookId: String) {
        // TODO: 实现阅读界面跳转
        Timber.d("准备打开阅读界面: $bookId")
        showToast("阅读功能即将实现")
    }
    
    /**
     * 处理返回键
     */
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
        
        if (currentFragment != null && fragmentManager.backStackEntryCount > 0) {
            // 如果有Fragment栈，返回上一个Fragment
            fragmentManager.popBackStack()
        } else {
            // 否则退出应用
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity销毁")
    }
} 