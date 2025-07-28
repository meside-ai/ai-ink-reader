package com.newbiechen.inkreader.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.usecases.book.GetAllBooksUseCase
import com.newbiechen.inkreader.domain.usecases.book.DeleteBookUseCase
import com.newbiechen.inkreader.domain.usecases.book.SearchBooksUseCase

/**
 * 图书列表ViewModel
 * 
 * 管理图书列表的状态和业务逻辑，包括：
 * - 图书列表数据管理
 * - 搜索功能
 * - 图书删除
 * - 列表状态管理
 */
@HiltViewModel
class BookListViewModel @Inject constructor(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val searchBooksUseCase: SearchBooksUseCase
) : BaseViewModel() {
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 是否处于搜索模式
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()
    
    // 搜索结果
    private val _searchResults = MutableLiveData<List<Book>>()
    val searchResults: LiveData<List<Book>> = _searchResults
    
    // 选中的图书（用于批量操作）
    private val _selectedBooks = MutableStateFlow<Set<String>>(emptySet())
    val selectedBooks: StateFlow<Set<String>> = _selectedBooks.asStateFlow()
    
    // 是否处于选择模式
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()
    
    // 列表为空状态
    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty
    
    // 图书列表数据（响应式）
    val books: LiveData<List<Book>> = try {
        getAllBooksUseCase().asLiveData().also { booksLiveData ->
            // 观察数据变化，更新空状态
            viewModelScope.launch {
                booksLiveData.observeForever { bookList ->
                    _isEmpty.postValue(bookList.isEmpty())
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "初始化图书列表失败")
        handleError(e)
        MutableLiveData<List<Book>>().apply { value = emptyList() }
    }
    
    init {
        Timber.d("BookListViewModel初始化")
    }
    
    /**
     * 开始搜索
     */
    fun startSearch() {
        _isSearchMode.value = true
        Timber.d("开启搜索模式")
    }
    
    /**
     * 停止搜索
     */
    fun stopSearch() {
        _isSearchMode.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        Timber.d("关闭搜索模式")
    }
    
    /**
     * 执行搜索
     */
    fun search(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        launchSafely(showLoading = false) {
            try {
                val result = searchBooksUseCase(SearchBooksUseCase.Params(query))
                result.fold(
                    onSuccess = { bookList ->
                        _searchResults.postValue(bookList)
                        Timber.d("搜索完成: 找到${bookList.size}本图书")
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "搜索失败")
                        _searchResults.postValue(emptyList())
                        handleError(exception)
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "搜索异常")
                _searchResults.postValue(emptyList())
                handleError(e)
            }
        }
    }
    
    /**
     * 删除图书
     */
    fun deleteBook(bookId: String) {
        launchSafely {
            try {
                val result = deleteBookUseCase(DeleteBookUseCase.Params(bookId))
                result.fold(
                    onSuccess = {
                        setSuccessMessage("图书删除成功")
                        Timber.i("图书删除成功: $bookId")
                        
                        // 如果是在选择模式中，移除选中状态
                        if (isSelectionMode.value) {
                            val currentSelected = _selectedBooks.value.toMutableSet()
                            currentSelected.remove(bookId)
                            _selectedBooks.value = currentSelected
                            
                            // 如果没有选中项了，退出选择模式
                            if (currentSelected.isEmpty()) {
                                exitSelectionMode()
                            }
                        }
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "删除图书失败: $bookId")
                        handleError(exception)
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "删除图书异常: $bookId")
                handleError(e)
            }
        }
    }
    
    /**
     * 批量删除选中的图书
     */
    fun deleteSelectedBooks() {
        val selectedBookIds = _selectedBooks.value
        if (selectedBookIds.isEmpty()) {
            setError("请先选择要删除的图书")
            return
        }
        
        launchSafely {
            try {
                var successCount = 0
                var failureCount = 0
                
                selectedBookIds.forEach { bookId ->
                    try {
                        val result = deleteBookUseCase(DeleteBookUseCase.Params(bookId))
                        result.fold(
                            onSuccess = { 
                                successCount++
                                Timber.d("删除图书成功: $bookId")
                            },
                            onFailure = { 
                                failureCount++
                                Timber.e("删除图书失败: $bookId")
                            }
                        )
                    } catch (e: Exception) {
                        failureCount++
                        Timber.e(e, "删除图书异常: $bookId")
                    }
                }
                
                // 清空选择状态
                _selectedBooks.value = emptySet()
                exitSelectionMode()
                
                // 显示结果
                val message = when {
                    failureCount == 0 -> "成功删除${successCount}本图书"
                    successCount == 0 -> "删除失败"
                    else -> "成功删除${successCount}本，失败${failureCount}本"
                }
                
                if (failureCount == 0) {
                    setSuccessMessage(message)
                } else {
                    setError(message)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "批量删除异常")
                handleError(e)
            }
        }
    }
    
    /**
     * 切换图书选中状态
     */
    fun toggleBookSelection(bookId: String) {
        val currentSelected = _selectedBooks.value.toMutableSet()
        
        if (currentSelected.contains(bookId)) {
            currentSelected.remove(bookId)
        } else {
            currentSelected.add(bookId)
        }
        
        _selectedBooks.value = currentSelected
        
        // 如果没有选中项，退出选择模式
        if (currentSelected.isEmpty()) {
            exitSelectionMode()
        }
        
        Timber.d("图书选择状态切换: $bookId, 当前选中: ${currentSelected.size}")
    }
    
    /**
     * 进入选择模式
     */
    fun enterSelectionMode(initialBookId: String? = null) {
        _isSelectionMode.value = true
        
        initialBookId?.let { bookId ->
            _selectedBooks.value = setOf(bookId)
        }
        
        Timber.d("进入选择模式")
    }
    
    /**
     * 退出选择模式
     */
    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedBooks.value = emptySet()
        Timber.d("退出选择模式")
    }
    
    /**
     * 全选
     */
    fun selectAllBooks() {
        val currentBooks = books.value ?: return
        _selectedBooks.value = currentBooks.map { it.bookId }.toSet()
        Timber.d("全选图书: ${currentBooks.size}本")
    }
    
    /**
     * 取消全选
     */
    fun deselectAllBooks() {
        _selectedBooks.value = emptySet()
        Timber.d("取消全选")
    }
    
    /**
     * 刷新图书列表
     */
    fun refreshBooks() {
        Timber.d("刷新图书列表")
        // 由于使用了Flow，数据会自动更新，这里主要用于日志记录
        // 如果需要强制刷新，可以在这里添加相应逻辑
    }
    
    /**
     * 检查图书是否被选中
     */
    fun isBookSelected(bookId: String): Boolean {
        return _selectedBooks.value.contains(bookId)
    }
    
    /**
     * 获取选中图书数量
     */
    fun getSelectedBookCount(): Int {
        return _selectedBooks.value.size
    }
} 