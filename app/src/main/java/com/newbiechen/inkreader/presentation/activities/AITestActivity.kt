package com.newbiechen.inkreader.presentation.activities

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
 * AI Test Activity
 * Simple testing interface for AI question functionality
 */
@AndroidEntryPoint
class AITestActivity : AppCompatActivity() {
    
    private val aiQuestionViewModel: AIQuestionViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set test context (independent AI testing - no database required)
        aiQuestionViewModel.setReadingContext(
            bookId = "ai_standalone_test",
            bookTitle = "AI功能独立测试",
            chapterTitle = "智能问答演示章节"
        )
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        // Title
        val titleText = TextView(this).apply {
            text = "AI问答功能测试"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(titleText)
        
        // Input text
        val inputLabel = TextView(this).apply {
            text = "测试文本："
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        rootLayout.addView(inputLabel)
        
        val inputEditText = EditText(this).apply {
            id = android.R.id.edit
            hint = "输入要测试的文本内容..."
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFEEEEEE.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 16
            }
            // Pre-fill with test data
            setText("人工智能(Artificial Intelligence，简称AI)是研究、开发用于模拟、延伸和扩展人的智能的理论、方法、技术及应用系统的一门新的技术科学。")
        }
        rootLayout.addView(inputEditText)
        
        // Quick test buttons
        val quickTestLabel = TextView(this).apply {
            text = "快速测试："
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }
        rootLayout.addView(quickTestLabel)
        
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Test buttons for each question type
        val questionTypes = listOf(
            AIQuestionType.CONCEPT_EXPLANATION,
            AIQuestionType.TRANSLATION,
            AIQuestionType.SUMMARY
        )
        
        questionTypes.forEach { type ->
            val button = Button(this).apply {
                text = type.displayName
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                ).apply {
                    marginEnd = 8
                }
                setOnClickListener {
                    val inputText = inputEditText.text.toString()
                    if (inputText.isNotBlank()) {
                        testAIQuestion(inputText, type)
                    } else {
                        Toast.makeText(this@AITestActivity, "请输入测试文本", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            buttonLayout.addView(button)
        }
        rootLayout.addView(buttonLayout)
        
        // English test button
        val englishTestButton = Button(this).apply {
            text = "测试英文翻译"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
            setOnClickListener {
                val englishText = "Machine learning is a subset of artificial intelligence that enables computers to learn and improve from experience without being explicitly programmed."
                inputEditText.setText(englishText)
                testAIQuestion(englishText, AIQuestionType.TRANSLATION)
            }
        }
        rootLayout.addView(englishTestButton)
        
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
        
        // Status text
        val statusText = TextView(this).apply {
            id = android.R.id.message
            text = "准备测试AI问答功能"
            textSize = 14f
            setPadding(0, 16, 0, 8)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        rootLayout.addView(statusText)
        
        // Result display
        val resultScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }
        
        val resultTextView = TextView(this).apply {
            id = android.R.id.text1
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
            visibility = android.view.View.GONE
        }
        resultScrollView.addView(resultTextView)
        rootLayout.addView(resultScrollView)
        
        // Close button
        val closeButton = Button(this).apply {
            text = "关闭测试"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            setOnClickListener { finish() }
        }
        rootLayout.addView(closeButton)
        
        setContentView(rootLayout)
    }
    
    private fun testAIQuestion(text: String, questionType: AIQuestionType) {
        aiQuestionViewModel.onTextSelected(text)
        aiQuestionViewModel.askQuestionWithType(questionType)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            aiQuestionViewModel.questionResult.collectLatest { result ->
                updateUIForResult(result)
            }
        }
    }
    
    private fun updateUIForResult(result: AIQuestionResult?) {
        val loadingIndicator = findViewById<ProgressBar>(android.R.id.progress)
        val statusText = findViewById<TextView>(android.R.id.message)
        val resultTextView = findViewById<TextView>(android.R.id.text1)
        
        when (result) {
            is AIQuestionResult.Loading -> {
                loadingIndicator.visibility = android.view.View.VISIBLE
                statusText.text = "AI正在思考中..."
                resultTextView.visibility = android.view.View.GONE
            }
            
            is AIQuestionResult.Success -> {
                loadingIndicator.visibility = android.view.View.GONE
                statusText.text = "✅ AI回答成功 - ${result.response.questionType.displayName}"
                resultTextView.visibility = android.view.View.VISIBLE
                resultTextView.text = result.response.answer
            }
            
            is AIQuestionResult.Error -> {
                loadingIndicator.visibility = android.view.View.GONE
                statusText.text = "❌ 错误"
                resultTextView.visibility = android.view.View.VISIBLE
                resultTextView.text = result.message
            }
            
            null -> {
                loadingIndicator.visibility = android.view.View.GONE
                statusText.text = "准备测试AI问答功能"
                resultTextView.visibility = android.view.View.GONE
            }
        }
    }
} 