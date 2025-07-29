package com.newbiechen.inkreader.presentation.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.newbiechen.inkreader.presentation.viewmodels.FilePickerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class EpubTestActivity : AppCompatActivity() {

    private val filePickerViewModel: FilePickerViewModel by viewModels()
    private lateinit var statusText: TextView
    private lateinit var testButton: Button

    // 权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            statusText.text = "权限已获取，可以开始测试"
            testButton.isEnabled = true
        } else {
            statusText.text = "需要存储权限才能测试EPUB导入"
            Toast.makeText(this, "需要存储权限才能访问文件", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createUI()
        observeViewModel()
        checkPermissions()
    }

    private fun createUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        statusText = TextView(this).apply {
            text = "准备测试EPUB文件导入..."
            textSize = 16f
            setPadding(0, 0, 0, 20)
        }

        testButton = Button(this).apply {
            text = "测试EPUB导入"
            textSize = 16f
            isEnabled = false // 默认禁用，等权限获取后启用
            setOnClickListener {
                testEpubImport()
            }
        }

        layout.addView(statusText)
        layout.addView(testButton)
        setContentView(layout)
    }

    private fun testEpubImport() {
        try {
            statusText.text = "开始测试EPUB导入..."
            Log.d("EpubTestActivity", "Starting EPUB import test")

            // 创建文件URI - 使用外部存储的测试文件
            val testFile = File("/sdcard/Download/test_book.epub")
            
            if (!testFile.exists()) {
                statusText.text = "错误：测试文件不存在 - ${testFile.absolutePath}"
                Log.e("EpubTestActivity", "Test file does not exist: ${testFile.absolutePath}")
                return
            }

            Log.d("EpubTestActivity", "Test file exists: ${testFile.absolutePath}, size: ${testFile.length()}")
            statusText.text = "找到测试文件，大小: ${testFile.length()} bytes"

            // 创建内容URI - 更兼容现代Android版本
            val fileUri = try {
                // 首先尝试使用 FileProvider
                androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    testFile
                )
            } catch (e: Exception) {
                Log.w("EpubTestActivity", "FileProvider failed, using file URI: ${e.message}")
                // 回退到file URI
                Uri.fromFile(testFile)
            }
            
            Log.d("EpubTestActivity", "Created URI: $fileUri")

            // 调用FilePickerViewModel处理文件
            filePickerViewModel.processSelectedFile(fileUri)
            
        } catch (e: Exception) {
            Log.e("EpubTestActivity", "Error in test", e)
            statusText.text = "测试出错: ${e.message}"
            Toast.makeText(this, "测试失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            filePickerViewModel.uiState.collectLatest { state ->
                Log.d("EpubTestActivity", "State update: loading=${state.isLoading}, error=${state.error}, bookId=${state.bookId}")
                
                when {
                    state.isLoading -> {
                        statusText.text = "正在处理EPUB文件..."
                        testButton.isEnabled = false
                    }
                    
                    state.error != null -> {
                        statusText.text = "错误: ${state.error}"
                        testButton.isEnabled = true
                        Toast.makeText(this@EpubTestActivity, "导入失败: ${state.error}", Toast.LENGTH_LONG).show()
                    }
                    
                    state.bookId != null -> {
                        statusText.text = "成功！图书ID: ${state.bookId}"
                        testButton.isEnabled = true
                        Toast.makeText(this@EpubTestActivity, "导入成功！", Toast.LENGTH_SHORT).show()
                    }
                    
                    else -> {
                        testButton.isEnabled = true
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            statusText.text = "权限已获取，可以开始测试"
            testButton.isEnabled = true
        } else {
            statusText.text = "正在请求存储权限..."
            requestPermissionLauncher.launch(permissions)
        }
    }
} 