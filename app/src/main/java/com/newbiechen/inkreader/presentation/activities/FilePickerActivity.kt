package com.newbiechen.inkreader.presentation.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newbiechen.inkreader.presentation.viewmodels.FilePickerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilePickerActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_RESULT_BOOK_ID = "result_book_id"
        
        fun createIntent(context: Context): Intent {
            return Intent(context, FilePickerActivity::class.java)
        }
        
        fun getBookIdFromResult(data: Intent?): String? {
            return data?.getStringExtra(EXTRA_RESULT_BOOK_ID)
        }
    }

    private val viewModel: FilePickerViewModel by viewModels()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.processSelectedFile(selectedUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Immediately show file picker
        showFilePicker()
        
        // Observe ViewModel state
        observeViewModel()
    }

    private fun showFilePicker() {
        filePickerLauncher.launch("*/*") // We'll filter in the ViewModel
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when {
                    state.isLoading -> {
                        // Show loading indicator if needed
                        // For now, just show toast
                        if (!state.hasShownLoadingToast) {
                            Toast.makeText(this@FilePickerActivity, "正在处理文件...", Toast.LENGTH_SHORT).show()
                            viewModel.markLoadingToastShown()
                        }
                    }
                    
                    state.error != null -> {
                        Toast.makeText(this@FilePickerActivity, "错误: ${state.error}", Toast.LENGTH_LONG).show()
                        finish() // Close activity on error
                    }
                    
                    state.bookId != null -> {
                        // File processed successfully
                        Toast.makeText(this@FilePickerActivity, "图书导入成功！", Toast.LENGTH_SHORT).show()
                        
                        // Return result
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_RESULT_BOOK_ID, state.bookId)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                    
                    else -> {
                        // Initial state or file picker was cancelled
                        if (state.isInitialized) {
                            finish() // User cancelled file selection
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }
} 