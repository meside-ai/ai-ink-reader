package com.newbiechen.inkreader.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbiechen.inkreader.core.ai.AIQuestionService
import com.newbiechen.inkreader.core.ai.models.AIQuestionRequest
import com.newbiechen.inkreader.core.ai.models.AIQuestionResult
import com.newbiechen.inkreader.core.ai.models.AIQuestionType
import com.newbiechen.inkreader.core.ai.models.AIConversationEntry
import com.newbiechen.inkreader.data.local.database.dao.AIConversationDao
import com.newbiechen.inkreader.data.local.database.entities.AIConversationEntity
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI Question ViewModel
 * Manages AI question-answer functionality and conversation history
 */
@HiltViewModel
class AIQuestionViewModel @Inject constructor(
    private val aiQuestionService: AIQuestionService,
    private val aiConversationDao: AIConversationDao
) : BaseViewModel() {
    
    // AI Question State
    private val _questionResult = MutableStateFlow<AIQuestionResult?>(null)
    val questionResult: StateFlow<AIQuestionResult?> = _questionResult.asStateFlow()
    
    // Current Question Context
    private val _currentBookId = MutableStateFlow<String?>(null)
    val currentBookId: StateFlow<String?> = _currentBookId.asStateFlow()
    
    private val _currentBookTitle = MutableStateFlow<String?>(null)
    val currentBookTitle: StateFlow<String?> = _currentBookTitle.asStateFlow()
    
    private val _currentChapterTitle = MutableStateFlow<String?>(null)
    val currentChapterTitle: StateFlow<String?> = _currentChapterTitle.asStateFlow()
    
    // Conversation History
    private val _conversationHistory = MutableStateFlow<List<AIConversationEntry>>(emptyList())
    val conversationHistory: StateFlow<List<AIConversationEntry>> = _conversationHistory.asStateFlow()
    
    // UI State
    private val _showAIDialog = MutableStateFlow(false)
    val showAIDialog: StateFlow<Boolean> = _showAIDialog.asStateFlow()
    
    private val _selectedText = MutableStateFlow<String?>(null)
    val selectedText: StateFlow<String?> = _selectedText.asStateFlow()
    
    private val _detectedQuestionType = MutableStateFlow<AIQuestionType?>(null)
    val detectedQuestionType: StateFlow<AIQuestionType?> = _detectedQuestionType.asStateFlow()
    
    /**
     * Exception handler for coroutines
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        _questionResult.value = AIQuestionResult.Error(
            message = "AI问答出现错误: ${exception.message}",
            throwable = exception
        )
    }
    
    /**
     * Set current reading context
     */
    fun setReadingContext(bookId: String, bookTitle: String, chapterTitle: String? = null) {
        _currentBookId.value = bookId
        _currentBookTitle.value = bookTitle
        _currentChapterTitle.value = chapterTitle
    }
    
    /**
     * Handle text selection and show AI dialog
     */
    fun onTextSelected(selectedText: String, context: String? = null) {
        _selectedText.value = selectedText
        val detectedType = aiQuestionService.detectQuestionType(selectedText)
        _detectedQuestionType.value = detectedType
        _showAIDialog.value = true
        
        // Clear previous result
        _questionResult.value = null
    }
    
    /**
     * Ask AI a question with automatic type detection
     */
    fun askQuestion(customQuestionType: AIQuestionType? = null) {
        val selectedText = _selectedText.value
        val bookId = _currentBookId.value
        val bookTitle = _currentBookTitle.value
        
        if (selectedText.isNullOrBlank() || bookId == null || bookTitle == null) {
            _questionResult.value = AIQuestionResult.Error("缺少必要的上下文信息")
            return
        }
        
        val questionType = customQuestionType ?: _detectedQuestionType.value 
            ?: AIQuestionType.CONCEPT_EXPLANATION
        
        val request = AIQuestionRequest(
            selectedText = selectedText,
            context = null, // TODO: Add context extraction from surrounding text
            questionType = questionType,
            bookId = bookId,
            chapterTitle = _currentChapterTitle.value
        )
        
        _questionResult.value = AIQuestionResult.Loading
        
        viewModelScope.launch(exceptionHandler) {
            val result = aiQuestionService.askQuestion(request)
            
            if (result.isSuccess) {
                val response = result.getOrThrow()
                _questionResult.value = AIQuestionResult.Success(response)
                
                // Save to conversation history
                saveConversationToHistory(response, bookTitle)
            } else {
                val exception = result.exceptionOrNull()
                _questionResult.value = AIQuestionResult.Error(
                    message = "AI回答失败: ${exception?.message ?: "未知错误"}",
                    throwable = exception
                )
            }
        }
    }
    
    /**
     * Ask question with specific type
     */
    fun askQuestionWithType(questionType: AIQuestionType) {
        askQuestion(questionType)
    }
    
    /**
     * Close AI dialog
     */
    fun closeAIDialog() {
        _showAIDialog.value = false
        _selectedText.value = null
        _detectedQuestionType.value = null
        _questionResult.value = null
    }
    
    /**
     * Load conversation history for current book
     */
    fun loadConversationHistory() {
        val bookId = _currentBookId.value ?: return
        
        viewModelScope.launch(exceptionHandler) {
            aiConversationDao.getConversationsByBook(bookId).collect { entities ->
                _conversationHistory.value = entities.map { entity ->
                    AIConversationEntry(
                        id = entity.id,
                        bookId = entity.bookId,
                        bookTitle = entity.bookTitle,
                        chapterTitle = entity.chapterTitle,
                        selectedText = entity.selectedText,
                        questionType = entity.getQuestionTypeEnum(),
                        answer = entity.answer,
                        timestamp = entity.timestamp,
                        isFromVoice = entity.isFromVoice
                    )
                }
            }
        }
    }
    
    /**
     * Search conversation history
     */
    fun searchConversationHistory(query: String) {
        if (query.isBlank()) {
            loadConversationHistory()
            return
        }
        
        viewModelScope.launch {
            aiConversationDao.searchConversations(query).collect { entities ->
                _conversationHistory.value = entities.map { entity ->
                    AIConversationEntry(
                        id = entity.id,
                        bookId = entity.bookId,
                        bookTitle = entity.bookTitle,
                        chapterTitle = entity.chapterTitle,
                        selectedText = entity.selectedText,
                        questionType = entity.getQuestionTypeEnum(),
                        answer = entity.answer,
                        timestamp = entity.timestamp,
                        isFromVoice = entity.isFromVoice
                    )
                }
            }
        }
    }
    
    /**
     * Delete conversation from history
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            val entity = aiConversationDao.getConversationById(conversationId)
            entity?.let {
                aiConversationDao.deleteConversation(it)
            }
        }
    }
    
    /**
     * Save conversation to history
     */
    private suspend fun saveConversationToHistory(response: com.newbiechen.inkreader.core.ai.models.AIQuestionResponse, bookTitle: String) {
        val bookId = _currentBookId.value ?: return
        
        // Skip saving for test contexts to avoid foreign key constraint errors
        if (bookId.startsWith("ai_") || bookId.startsWith("test")) {
            android.util.Log.d("AIQuestionViewModel", "跳过测试模式下的对话历史保存: bookId=$bookId")
            return
        }
        
        try {
            val entity = AIConversationEntity(
                id = response.id,
                bookId = bookId,
                bookTitle = bookTitle,
                chapterTitle = _currentChapterTitle.value,
                selectedText = response.selectedText,
                questionType = response.questionType.name,
                answer = response.answer,
                timestamp = response.timestamp,
                isFromVoice = false
            )
            
            aiConversationDao.insertConversation(entity)
            android.util.Log.d("AIQuestionViewModel", "对话历史保存成功: ${entity.id}")
        } catch (e: Exception) {
            android.util.Log.e("AIQuestionViewModel", "保存对话历史失败: ${e.message}", e)
            // Don't rethrow - conversation saving failure shouldn't break the AI functionality
        }
    }
    
    /**
     * Get available question types
     */
    fun getAvailableQuestionTypes(): List<AIQuestionType> {
        return AIQuestionType.values().toList()
    }
    
    /**
     * Clear current question result
     */
    fun clearQuestionResult() {
        _questionResult.value = null
    }
} 