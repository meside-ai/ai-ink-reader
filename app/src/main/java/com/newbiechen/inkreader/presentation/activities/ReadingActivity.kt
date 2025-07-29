package com.newbiechen.inkreader.presentation.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newbiechen.inkreader.R
import com.newbiechen.inkreader.presentation.viewmodels.ReadingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class ReadingActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_BOOK_ID = "book_id"
        
        fun createIntent(context: Context, bookId: String): Intent {
            return Intent(context, ReadingActivity::class.java).apply {
                putExtra(EXTRA_BOOK_ID, bookId)
            }
        }
    }

    private val viewModel: ReadingViewModel by viewModels()
    
    private lateinit var webView: WebView
    private lateinit var statusBar: TextView
    private lateinit var gestureDetector: GestureDetector
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val bookId = intent.getStringExtra(EXTRA_BOOK_ID)
        if (bookId == null) {
            Toast.makeText(this, "无效的图书ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        createUI()
        setupWebView()
        setupGestureDetector()
        observeViewModel()
        
        viewModel.loadBook(bookId)
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun createUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // WebView for EPUB content
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // Take remaining space
            )
        }
        
        // Status bar showing current page/chapter info
        statusBar = TextView(this).apply {
            text = "正在加载..."
            textSize = 12f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(16, 8, 16, 8)
            setBackgroundColor(getColor(R.color.status_bar_background))
        }
        
        mainLayout.addView(webView)
        mainLayout.addView(statusBar)
        
        setContentView(mainLayout)
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                builtInZoomControls = false
                displayZoomControls = false
                setSupportZoom(false)
                textZoom = 100
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    // Inject CSS for better reading experience and text selection
                    val css = """
                        <style>
                            body {
                                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                line-height: 1.6;
                                padding: 20px;
                                margin: 0;
                                color: #333;
                                background-color: #fff;
                                word-break: break-word;
                                -webkit-touch-callout: none;
                                -webkit-user-select: text;
                                -khtml-user-select: text;
                                -moz-user-select: text;
                                -ms-user-select: text;
                                user-select: text;
                            }
                            
                            p {
                                margin: 12px 0;
                                text-align: justify;
                            }
                            
                            h1, h2, h3, h4, h5, h6 {
                                margin: 20px 0 10px 0;
                                font-weight: bold;
                            }
                            
                            img {
                                max-width: 100%;
                                height: auto;
                                display: block;
                                margin: 16px auto;
                            }
                            
                            /* Text selection styles */
                            ::selection {
                                background-color: #007AFF;
                                color: white;
                            }
                            
                            ::-moz-selection {
                                background-color: #007AFF;
                                color: white;
                            }
                        </style>
                    """.trimIndent()
                    
                    webView.evaluateJavascript(
                        """
                        var style = document.createElement('style');
                        style.innerHTML = `$css`;
                        document.head.appendChild(style);
                        """.trimIndent(),
                        null
                    )
                    
                    viewModel.onPageLoaded()
                }
            }
            
            // Enable text selection
            setOnLongClickListener { 
                // Let WebView handle long clicks for text selection
                false
            }
        }
    }
    
    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val screenWidth = webView.width
                val tapX = e.x
                
                // Divide screen into 3 areas: left (previous), center (menu), right (next)
                when {
                    tapX < screenWidth * 0.3 -> {
                        // Left area - previous page
                        viewModel.previousPage()
                        return true
                    }
                    tapX > screenWidth * 0.7 -> {
                        // Right area - next page
                        viewModel.nextPage()
                        return true
                    }
                    else -> {
                        // Center area - show menu (future implementation)
                        return false
                    }
                }
            }
            
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                
                // Only handle horizontal swipes
                if (abs(diffX) > abs(diffY) && abs(diffX) > 100 && abs(velocityX) > 100) {
                    if (diffX > 0) {
                        // Swipe right - previous page
                        viewModel.previousPage()
                    } else {
                        // Swipe left - next page
                        viewModel.nextPage()
                    }
                    return true
                }
                
                return false
            }
        })
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when {
                    state.isLoading -> {
                        statusBar.text = "正在加载..."
                    }
                    
                    state.error != null -> {
                        statusBar.text = "错误: ${state.error}"
                        Toast.makeText(this@ReadingActivity, state.error, Toast.LENGTH_LONG).show()
                    }
                    
                    state.currentChapter != null -> {
                        loadChapterContent(state.currentChapter.htmlContent)
                        updateStatusBar(state)
                    }
                }
            }
        }
    }
    
    private fun loadChapterContent(htmlContent: String) {
        val htmlWithMeta = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chapter</title>
            </head>
            <body>
                $htmlContent
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, htmlWithMeta, "text/html", "UTF-8", null)
    }
    
    private fun updateStatusBar(state: ReadingViewModel.ReadingUiState) {
        val book = state.book
        val chapter = state.currentChapter
        
        if (book != null && chapter != null) {
            val progress = "${state.currentChapterIndex + 1} / ${state.totalChapters}"
            statusBar.text = "${book.title} - ${chapter.title} ($progress)"
        }
    }
} 