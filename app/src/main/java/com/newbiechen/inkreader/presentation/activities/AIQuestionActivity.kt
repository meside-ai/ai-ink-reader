package com.newbiechen.inkreader.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newbiechen.inkreader.core.ai.models.AIQuestionResult
import com.newbiechen.inkreader.core.ai.models.AIQuestionType
import com.newbiechen.inkreader.presentation.viewmodels.AIQuestionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * AI Question Activity
 * Displays AI question-answer interface optimized for e-ink screens
 */
@AndroidEntryPoint
class AIQuestionActivity : AppCompatActivity() {
    
    private val aiQuestionViewModel: AIQuestionViewModel by viewModels()
    
    companion object {
        private const val EXTRA_SELECTED_TEXT = "selected_text"
        private const val EXTRA_BOOK_ID = "book_id"
        private const val EXTRA_BOOK_TITLE = "book_title"
        private const val EXTRA_CHAPTER_TITLE = "chapter_title"
        
        fun createIntent(
            context: Context,
            selectedText: String,
            bookId: String,
            bookTitle: String,
            chapterTitle: String? = null
        ): Intent {
            return Intent(context, AIQuestionActivity::class.java).apply {
                putExtra(EXTRA_SELECTED_TEXT, selectedText)
                putExtra(EXTRA_BOOK_ID, bookId)
                putExtra(EXTRA_BOOK_TITLE, bookTitle)
                putExtra(EXTRA_CHAPTER_TITLE, chapterTitle)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val selectedText = intent.getStringExtra(EXTRA_SELECTED_TEXT) ?: ""
        val bookId = intent.getStringExtra(EXTRA_BOOK_ID) ?: ""
        val bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: ""
        val chapterTitle = intent.getStringExtra(EXTRA_CHAPTER_TITLE)
        
        // Set context and text selection
        aiQuestionViewModel.setReadingContext(bookId, bookTitle, chapterTitle)
        aiQuestionViewModel.onTextSelected(selectedText)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Create UI programmatically for e-ink optimization
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "AI智能问答"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        rootLayout.addView(titleText)
        
        // Selected text display
        val selectedTextLabel = TextView(this).apply {
            text = "选中文本："
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        rootLayout.addView(selectedTextLabel)
        
        val selectedTextView = TextView(this).apply {
            id = android.R.id.text1
            textSize = 16f
            setPadding(16, 8, 16, 16)
            setBackgroundColor(0xFFEEEEEE.toInt())
            maxLines = 5
        }
        rootLayout.addView(selectedTextView)
        
        // Question type selection
        val questionTypeLabel = TextView(this).apply {
            text = "问题类型："
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
        rootLayout.addView(questionTypeLabel)
        
        val questionTypeSpinner = Spinner(this).apply {
            id = android.R.id.list
        }
        rootLayout.addView(questionTypeSpinner)
        
        // Ask button
        val askButton = Button(this).apply {
            id = android.R.id.button1
            text = "开始提问"
            textSize = 16f
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }
        rootLayout.addView(askButton)
        
        // Loading indicator
        val loadingIndicator = ProgressBar(this).apply {
            id = android.R.id.progress
            visibility = android.view.View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                topMargin = 16
            }
        }
        rootLayout.addView(loadingIndicator)
        
        // Answer display
        val answerScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            ).apply {
                topMargin = 16
            }
        }
        
        val answerTextView = TextView(this).apply {
            id = android.R.id.text2
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
            visibility = android.view.View.GONE
        }
        answerScrollView.addView(answerTextView)
        rootLayout.addView(answerScrollView)
        
        // Close button
        val closeButton = Button(this).apply {
            id = android.R.id.button2
            text = "关闭"
            textSize = 16f
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }
        rootLayout.addView(closeButton)
        
        setContentView(rootLayout)
        
        // Setup question type spinner
        setupQuestionTypeSpinner()
        
        // Setup button listeners
        askButton.setOnClickListener {
            val questionTypes = questionTypeSpinner.tag as? List<AIQuestionType>
            val selectedIndex = questionTypeSpinner.selectedItemPosition
            val selectedType = questionTypes?.getOrNull(selectedIndex)
            aiQuestionViewModel.askQuestion(selectedType)
        }
        
        closeButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupQuestionTypeSpinner() {
        val questionTypeSpinner = findViewById<Spinner>(android.R.id.list)
        val questionTypes = aiQuestionViewModel.getAvailableQuestionTypes()
        
        // Create simple adapter for display
        val displayTexts = questionTypes.map { "${it.displayName} - ${it.description}" }
        val adapter = ArrayAdapter(
            this@AIQuestionActivity,
            android.R.layout.simple_spinner_item,
            displayTexts
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionTypeSpinner.adapter = adapter
        
        // Store question types mapping for later retrieval
        questionTypeSpinner.tag = questionTypes
    }
    
    private fun observeViewModel() {
        // Observe selected text
        lifecycleScope.launch {
            aiQuestionViewModel.selectedText.collectLatest { selectedText ->
                findViewById<TextView>(android.R.id.text1).text = selectedText ?: ""
            }
        }
        
        // Observe detected question type
        lifecycleScope.launch {
            aiQuestionViewModel.detectedQuestionType.collectLatest { detectedType ->
                detectedType?.let {
                    val questionTypes = aiQuestionViewModel.getAvailableQuestionTypes()
                    val index = questionTypes.indexOf(it)
                    if (index >= 0) {
                        findViewById<Spinner>(android.R.id.list).setSelection(index)
                    }
                }
            }
        }
        
        // Observe question result
        lifecycleScope.launch {
            aiQuestionViewModel.questionResult.collectLatest { result ->
                updateUIForResult(result)
            }
        }
    }
    
    private fun updateUIForResult(result: AIQuestionResult?) {
        val loadingIndicator = findViewById<ProgressBar>(android.R.id.progress)
        val answerTextView = findViewById<TextView>(android.R.id.text2)
        val askButton = findViewById<Button>(android.R.id.button1)
        
        when (result) {
            is AIQuestionResult.Loading -> {
                loadingIndicator.visibility = android.view.View.VISIBLE
                answerTextView.visibility = android.view.View.GONE
                askButton.isEnabled = false
                askButton.text = "正在思考..."
            }
            
            is AIQuestionResult.Success -> {
                loadingIndicator.visibility = android.view.View.GONE
                answerTextView.visibility = android.view.View.VISIBLE
                answerTextView.text = "【${result.response.questionType.displayName}】\n\n${result.response.answer}"
                askButton.isEnabled = true
                askButton.text = "重新提问"
            }
            
            is AIQuestionResult.Error -> {
                loadingIndicator.visibility = android.view.View.GONE
                answerTextView.visibility = android.view.View.VISIBLE
                answerTextView.text = "❌ 错误：${result.message}"
                askButton.isEnabled = true
                askButton.text = "重试"
            }
            
            null -> {
                loadingIndicator.visibility = android.view.View.GONE
                answerTextView.visibility = android.view.View.GONE
                askButton.isEnabled = true
                askButton.text = "开始提问"
            }
        }
    }
} 