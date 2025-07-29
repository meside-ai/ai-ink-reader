package com.newbiechen.inkreader.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import com.newbiechen.inkreader.domain.usecases.book.GetBookByIdUseCase
import com.newbiechen.inkreader.domain.usecases.book.GetBookChaptersUseCase
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 图书详情ViewModel
 */
@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getBookChaptersUseCase: GetBookChaptersUseCase
) : BaseViewModel() {
    
    // 当前图书ID
    private val _currentBookId = MutableStateFlow<String?>(null)
    
    // 图书详情
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()
    
    // 章节列表
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()
    
    /**
     * 加载图书详情
     */
    fun loadBookDetail(bookId: String) {
        if (_currentBookId.value == bookId) {
            // 已经加载过相同的图书，不重复加载
            return
        }
        
        _currentBookId.value = bookId
        
        launchSafe {
            // 并行加载图书信息和章节列表
            val bookResult = getBookByIdUseCase(bookId)
            val chaptersResult = getBookChaptersUseCase(bookId)
            
            bookResult.onSuccess { book ->
                if (book != null) {
                    _book.value = book
                } else {
                    showError("图书不存在或已被删除")
                }
            }.onFailure { exception ->
                showError("加载图书信息失败: ${exception.message}")
            }
            
            chaptersResult.onSuccess { chapters ->
                _chapters.value = chapters
            }.onFailure { exception ->
                showError("加载章节列表失败: ${exception.message}")
            }
        }
    }
    
    /**
     * 刷新图书详情
     */
    fun refresh() {
        _currentBookId.value?.let { bookId ->
            loadBookDetail(bookId)
        }
    }
    
    /**
     * 获取指定章节
     */
    fun getChapter(chapterId: String): Chapter? {
        return _chapters.value.find { it.chapterId == chapterId }
    }
    
    /**
     * 获取下一章节
     */
    fun getNextChapter(currentChapterId: String): Chapter? {
        val chapters = _chapters.value
        val currentIndex = chapters.indexOfFirst { it.chapterId == currentChapterId }
        return if (currentIndex >= 0 && currentIndex < chapters.size - 1) {
            chapters[currentIndex + 1]
        } else {
            null
        }
    }
    
    /**
     * 获取上一章节
     */
    fun getPreviousChapter(currentChapterId: String): Chapter? {
        val chapters = _chapters.value
        val currentIndex = chapters.indexOfFirst { it.chapterId == currentChapterId }
        return if (currentIndex > 0) {
            chapters[currentIndex - 1]
        } else {
            null
        }
    }
    
    /**
     * 更新最后阅读时间（当用户开始阅读时调用）
     */
    fun updateLastReadTime() {
        val book = _book.value ?: return
        _book.value = book.copy(lastOpenedAt = System.currentTimeMillis())
    }
} 