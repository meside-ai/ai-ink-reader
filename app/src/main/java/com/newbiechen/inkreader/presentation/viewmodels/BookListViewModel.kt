package com.newbiechen.inkreader.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.usecases.book.*
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 图书列表ViewModel
 */
@HiltViewModel
class BookListViewModel @Inject constructor(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val addBookUseCase: AddBookUseCase
) : BaseViewModel() {
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 所有图书列表
    private val allBooks = getAllBooksUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 搜索结果
    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    
    // 当前显示的图书列表（根据搜索状态动态切换）
    val books: StateFlow<List<Book>> = combine(
        allBooks,
        _searchResults,
        searchQuery
    ) { allBooks, searchResults, query ->
        if (query.isBlank()) {
            allBooks
        } else {
            searchResults
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 选中的图书ID集合
    private val _selectedBookIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBookIds: StateFlow<Set<String>> = _selectedBookIds.asStateFlow()
    
    // 是否处于选择模式
    val isSelectionMode = selectedBookIds.map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    // 已选择的图书数量
    val selectedCount = selectedBookIds.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    /**
     * 搜索图书
     */
    fun searchBooks(query: String) {
        _searchQuery.value = query.trim()
        
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        launchSafe(showLoading = false) {
            val result = searchBooksUseCase(query)
            result.onSuccess { searchResults ->
                _searchResults.value = searchResults
            }.onFailure { exception ->
                showError("搜索失败: ${exception.message}")
                _searchResults.value = emptyList()
            }
        }
    }
    
    /**
     * 清除搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
    
    /**
     * 选择/取消选择图书
     */
    fun toggleBookSelection(bookId: String) {
        val currentSelection = _selectedBookIds.value.toMutableSet()
        if (currentSelection.contains(bookId)) {
            currentSelection.remove(bookId)
        } else {
            currentSelection.add(bookId)
        }
        _selectedBookIds.value = currentSelection
    }
    
    /**
     * 全选图书
     */
    fun selectAllBooks() {
        val currentBooks = books.value
        _selectedBookIds.value = currentBooks.map { it.bookId }.toSet()
    }
    
    /**
     * 清除所有选择
     */
    fun clearSelection() {
        _selectedBookIds.value = emptySet()
    }
    
    /**
     * 删除选中的图书
     */
    fun deleteSelectedBooks() {
        val selectedIds = _selectedBookIds.value
        if (selectedIds.isEmpty()) return
        
        launchSafe {
            var successCount = 0
            var failureCount = 0
            
            selectedIds.forEach { bookId ->
                val result = deleteBookUseCase(bookId)
                result.onSuccess {
                    successCount++
                }.onFailure {
                    failureCount++
                }
            }
            
            // 清除选择状态
            _selectedBookIds.value = emptySet()
            
            // 显示结果消息
            when {
                failureCount == 0 -> {
                    // 全部成功，不显示消息（用户能看到列表变化）
                }
                successCount == 0 -> {
                    showError("删除失败，共 ${selectedIds.size} 本图书删除失败")
                }
                else -> {
                    showError("部分删除成功：${successCount} 本成功，${failureCount} 本失败")
                }
            }
        }
    }
    
    /**
     * 添加示例图书（用于测试）
     */
    fun addSampleBook() {
        launchSafe {
            val sampleBook = Book(
                bookId = "sample_${System.currentTimeMillis()}",
                filePath = "/sample/test_book.epub",
                title = "测试图书 ${System.currentTimeMillis() % 1000}",
                author = "测试作者",
                publisher = "测试出版社",
                totalChapters = 5
            )
            
            val result = addBookUseCase(sampleBook)
            result.onSuccess {
                // 成功添加，列表会自动更新
            }.onFailure { exception ->
                showError("添加图书失败: ${exception.message}")
            }
        }
    }
    
    /**
     * 刷新图书列表
     */
    fun refresh() {
        // 由于使用了Flow，数据会自动刷新
        // 这里可以添加手动刷新逻辑，比如重新加载示例数据
        android.util.Log.d("BookListViewModel", "刷新图书列表")
    }
} 