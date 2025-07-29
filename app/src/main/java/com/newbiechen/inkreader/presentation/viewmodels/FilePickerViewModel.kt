package com.newbiechen.inkreader.presentation.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.newbiechen.inkreader.core.epub.EpubParserService
import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.usecases.book.AddBookUseCase
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

data class FilePickerUiState(
    val isLoading: Boolean = false,
    val bookId: String? = null,
    val error: String? = null,
    val isInitialized: Boolean = false,
    val hasShownLoadingToast: Boolean = false
)

@HiltViewModel
class FilePickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val epubParserService: EpubParserService,
    private val addBookUseCase: AddBookUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FilePickerUiState())
    val uiState: StateFlow<FilePickerUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(isInitialized = true)
    }

    fun processSelectedFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // First, validate if it's an EPUB file
                val fileName = getFileName(uri)
                android.util.Log.d("FilePickerViewModel", "Processing file: $fileName from URI: $uri")
                
                if (!fileName.lowercase().endsWith(".epub")) {
                    throw Exception("请选择 EPUB 格式的文件")
                }

                // Copy file to app's internal storage
                android.util.Log.d("FilePickerViewModel", "Copying file to internal storage...")
                val copiedFilePath = copyFileToInternalStorage(uri, fileName)
                android.util.Log.d("FilePickerViewModel", "File copied to: $copiedFilePath")
                
                // Validate EPUB file
                android.util.Log.d("FilePickerViewModel", "Validating EPUB file...")
                val validationResult = epubParserService.validateEpubFile(copiedFilePath)
                android.util.Log.d("FilePickerViewModel", "Validation result: ${validationResult.isValid}, error: ${validationResult.errorMessage}")
                
                if (!validationResult.isValid) {
                    // Clean up copied file
                    File(copiedFilePath).delete()
                    throw Exception(validationResult.errorMessage ?: "无效的 EPUB 文件")
                }

                // Parse EPUB metadata
                android.util.Log.d("FilePickerViewModel", "Parsing EPUB metadata...")
                val metadataResult = epubParserService.parseEpub(copiedFilePath)
                if (metadataResult.isFailure) {
                    // Clean up copied file
                    File(copiedFilePath).delete()
                    android.util.Log.e("FilePickerViewModel", "Parse failure", metadataResult.exceptionOrNull())
                    throw Exception("解析 EPUB 文件失败: ${metadataResult.exceptionOrNull()?.message}")
                }

                val metadata = metadataResult.getOrThrow()

                // Extract cover image if available
                val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
                val coverImagePath = epubParserService.extractCoverImage(copiedFilePath, coversDir.absolutePath)
                    .getOrNull()

                // Create Book entity
                val bookId = UUID.randomUUID().toString()
                val book = Book(
                    bookId = bookId,
                    filePath = copiedFilePath,
                    title = metadata.title,
                    author = metadata.author,
                    publisher = metadata.publisher,
                    language = metadata.language,
                    coverImagePath = coverImagePath,
                    fileSize = File(copiedFilePath).length(),
                    totalChapters = metadata.totalChapters,
                    createdAt = System.currentTimeMillis(),
                    lastOpenedAt = System.currentTimeMillis(),
                    isDeleted = false
                )

                // Add book to database
                android.util.Log.d("FilePickerViewModel", "Adding book to database: ${book.title}")
                val addResult = addBookUseCase(book)
                if (addResult.isFailure) {
                    // Clean up files
                    File(copiedFilePath).delete()
                    coverImagePath?.let { File(it).delete() }
                    android.util.Log.e("FilePickerViewModel", "Add book failure", addResult.exceptionOrNull())
                    throw Exception("添加图书失败: ${addResult.exceptionOrNull()?.message}")
                }

                android.util.Log.d("FilePickerViewModel", "Book successfully added with ID: $bookId")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bookId = bookId,
                    error = null
                )

            } catch (e: Exception) {
                android.util.Log.e("FilePickerViewModel", "Error processing file", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "处理文件时发生未知错误"
                )
            }
        }
    }

    fun markLoadingToastShown() {
        _uiState.value = _uiState.value.copy(hasShownLoadingToast = true)
    }

    private suspend fun copyFileToInternalStorage(uri: Uri, fileName: String): String {
        val booksDir = File(context.filesDir, "books").apply { mkdirs() }
        val destinationFile = File(booksDir, "${UUID.randomUUID()}_$fileName")

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("无法读取选择的文件")

        return destinationFile.absolutePath
    }

    private fun getFileName(uri: Uri): String {
        return when (uri.scheme) {
            "content" -> {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else null
                } ?: "unknown.epub"
            }
            "file" -> {
                File(uri.path ?: "").name
            }
            else -> "unknown.epub"
        }
    }
} 