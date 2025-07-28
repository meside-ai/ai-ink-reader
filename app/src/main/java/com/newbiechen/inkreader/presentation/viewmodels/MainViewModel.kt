package com.newbiechen.inkreader.presentation.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.newbiechen.inkreader.presentation.viewmodels.base.BaseViewModel
import com.newbiechen.inkreader.core.files.FileManagerService
import com.newbiechen.inkreader.core.files.FileImportResult
import com.newbiechen.inkreader.domain.usecases.book.AddBookUseCase

/**
 * 主界面ViewModel
 * 
 * 负责主界面的业务逻辑，特别是文件导入和管理
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileManagerService: FileManagerService,
    private val addBookUseCase: AddBookUseCase
) : BaseViewModel() {
    
    /**
     * 创建文件选择器Intent
     */
    fun createFilePickerIntent(): Intent {
        return fileManagerService.createFilePickerIntent()
    }
    
    /**
     * 导入文件
     */
    suspend fun importFile(uri: Uri): FileImportResult {
        Timber.d("开始导入文件: $uri")
        
        return try {
            // 第一步：复制文件到应用目录
            when (val copyResult = fileManagerService.copyFileFromUri(uri)) {
                is FileImportResult.Success -> {
                    Timber.d("文件复制成功: ${copyResult.filePath}")
                    
                    // 第二步：解析EPUB并保存到数据库
                    try {
                        val addResult = addBookUseCase(AddBookUseCase.Params(copyResult.filePath))
                        
                        addResult.fold(
                            onSuccess = { book ->
                                Timber.i("图书添加成功: ${book.title}")
                                FileImportResult.Success(copyResult.filePath, copyResult.fileInfo)
                            },
                            onFailure = { exception ->
                                Timber.e(exception, "图书添加失败")
                                
                                // 如果数据库操作失败，清理已复制的文件
                                cleanupFailedImport(copyResult.filePath)
                                
                                FileImportResult.Error("EPUB解析失败: ${exception.message}")
                            }
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "图书添加异常")
                        cleanupFailedImport(copyResult.filePath)
                        FileImportResult.Error("导入异常: ${e.message}")
                    }
                }
                
                is FileImportResult.Error -> {
                    Timber.e("文件复制失败: ${copyResult.message}")
                    copyResult
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "导入文件异常")
            FileImportResult.Error("导入失败: ${e.message}")
        }
    }
    
    /**
     * 清理导入失败的文件
     */
    private suspend fun cleanupFailedImport(filePath: String) {
        try {
            val deleted = fileManagerService.deleteFile(filePath)
            if (deleted) {
                Timber.d("清理失败的导入文件: $filePath")
            } else {
                Timber.w("清理文件失败: $filePath")
            }
        } catch (e: Exception) {
            Timber.e(e, "清理文件异常: $filePath")
        }
    }
    
    /**
     * 获取应用统计信息
     */
    fun getAppStatistics() {
        launchSafely(showLoading = false) {
            try {
                val epubDir = fileManagerService.getEpubDirectory()
                val totalSize = fileManagerService.getDirectorySize(epubDir)
                
                Timber.d("应用统计 - EPUB目录大小: ${totalSize}字节")
                
                // 这里可以发送统计信息到UI
                // 可以通过LiveData暴露统计数据
                
            } catch (e: Exception) {
                Timber.e(e, "获取统计信息失败")
            }
        }
    }
    
    /**
     * 清理缓存
     */
    fun cleanupCache() {
        launchSafely {
            try {
                val result = fileManagerService.cleanupCache()
                
                val message = if (result.deletedCount > 0) {
                    "清理完成，删除${result.deletedCount}个文件，释放${result.getFormattedDeletedSize()}"
                } else {
                    "暂无需要清理的文件"
                }
                
                setSuccessMessage(message)
                Timber.i("缓存清理完成: $result")
                
            } catch (e: Exception) {
                Timber.e(e, "清理缓存失败")
                setError("清理缓存失败")
            }
        }
    }
} 