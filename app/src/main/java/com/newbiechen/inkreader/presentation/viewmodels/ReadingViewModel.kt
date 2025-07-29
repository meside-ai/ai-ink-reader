package com.newbiechen.inkreader.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.newbiechen.inkreader.core.epub.EpubParserService
import com.newbiechen.inkreader.core.epub.models.EpubChapter
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.usecases.book.GetBookByIdUseCase
import com.newbiechen.inkreader.domain.usecases.book.GetBookChaptersUseCase
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getBookChaptersUseCase: GetBookChaptersUseCase,
    private val epubParserService: EpubParserService
) : BaseViewModel() {

    data class ReadingUiState(
        val isLoading: Boolean = false,
        val book: Book? = null,
        val chapters: List<EpubChapter> = emptyList(),
        val currentChapter: EpubChapter? = null,
        val currentChapterIndex: Int = 0,
        val totalChapters: Int = 0,
        val error: String? = null,
        val isPageLoaded: Boolean = false
    )

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load book from database
                val book = getBookByIdUseCase(bookId).getOrNull()
                if (book == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "图书不存在"
                    )
                    return@launch
                }

                // Load chapters - check if it's internal sample data or real EPUB
                val chapters = if (book.filePath.startsWith("internal://")) {
                    // Load from database for sample data
                    val dbChapters = getBookChaptersUseCase(bookId).getOrNull() ?: emptyList()
                    dbChapters.map { chapter ->
                        EpubChapter(
                            id = chapter.chapterId,
                            title = chapter.title,
                            href = "${chapter.chapterId}.html",
                            order = chapter.order - 1, // Convert to 0-based index
                            htmlContent = chapter.content,
                            wordCount = chapter.wordCount
                        )
                    }
                } else {
                    // Extract from EPUB file
                    val chaptersResult = epubParserService.extractChapters(book.filePath)
                    if (chaptersResult.isFailure) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "无法加载图书章节: ${chaptersResult.exceptionOrNull()?.message}"
                        )
                        return@launch
                    }
                    chaptersResult.getOrThrow()
                }

                if (chapters.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "图书章节为空"
                    )
                    return@launch
                }

                // Load first chapter
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    book = book,
                    chapters = chapters,
                    currentChapter = chapters.first(),
                    currentChapterIndex = 0,
                    totalChapters = chapters.size,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载图书失败: ${e.message}"
                )
            }
        }
    }

    fun nextPage() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentChapterIndex + 1
        
        if (nextIndex < currentState.totalChapters) {
            loadChapter(nextIndex)
        }
    }

    fun previousPage() {
        val currentState = _uiState.value
        val previousIndex = currentState.currentChapterIndex - 1
        
        if (previousIndex >= 0) {
            loadChapter(previousIndex)
        }
    }

    fun jumpToChapter(chapterIndex: Int) {
        val currentState = _uiState.value
        if (chapterIndex in 0 until currentState.totalChapters) {
            loadChapter(chapterIndex)
        }
    }

    fun onPageLoaded() {
        _uiState.value = _uiState.value.copy(isPageLoaded = true)
    }

    private fun loadChapter(chapterIndex: Int) {
        val currentState = _uiState.value
        if (chapterIndex in currentState.chapters.indices) {
            val chapter = currentState.chapters[chapterIndex]
            
            _uiState.value = currentState.copy(
                currentChapter = chapter,
                currentChapterIndex = chapterIndex,
                isPageLoaded = false
            )
        }
    }

    fun getCurrentChapterText(): String? {
        return _uiState.value.currentChapter?.htmlContent
    }

    fun getBookTitle(): String? {
        return _uiState.value.book?.title
    }

    fun getChapterTitle(): String? {
        return _uiState.value.currentChapter?.title
    }

    fun getReadingProgress(): Pair<Int, Int> {
        val state = _uiState.value
        return Pair(state.currentChapterIndex + 1, state.totalChapters)
    }
} 