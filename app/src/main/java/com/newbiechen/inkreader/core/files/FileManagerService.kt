package com.newbiechen.inkreader.core.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton
import com.newbiechen.inkreader.utils.runSafely

/**
 * 文件管理服务
 * 
 * 处理EPUB文件的选择、导入、复制和管理功能
 */
@Singleton
class FileManagerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val EPUB_MIME_TYPE = "application/epub+zip"
        private const val EPUB_CACHE_DIR = "epub_books"
        private const val MAX_FILE_SIZE = 100 * 1024 * 1024L // 100MB
    }
    
    /**
     * 创建文件选择Intent
     */
    fun createFilePickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = EPUB_MIME_TYPE
            
            // 备用MIME类型，以防某些文件管理器不支持
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                EPUB_MIME_TYPE,
                "application/octet-stream",
                "*/*"
            ))
            
            // 添加额外的过滤条件
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, 
                DocumentsContract.buildRootsUri(DocumentsContract.AUTHORITY))
        }
    }
    
    /**
     * 从URI复制文件到应用私有目录
     */
    suspend fun copyFileFromUri(uri: Uri): FileImportResult = withContext(Dispatchers.IO) {
        runSafely {
            Timber.d("开始从URI导入文件: $uri")
            
            // 获取文件信息
            val fileInfo = getFileInfoFromUri(uri)
            if (fileInfo == null) {
                return@runSafely FileImportResult.Error("无法获取文件信息")
            }
            
            // 验证文件
            val validationResult = validateFile(fileInfo)
            if (!validationResult.isValid) {
                return@runSafely FileImportResult.Error(validationResult.errorMessage)
            }
            
            // 创建目标文件路径
            val targetFile = createTargetFile(fileInfo.fileName)
            
            // 检查文件是否已存在
            if (targetFile.exists()) {
                return@runSafely FileImportResult.Error("文件已存在: ${fileInfo.fileName}")
            }
            
            // 复制文件
            val copiedFile = copyFile(uri, targetFile)
            
            Timber.i("文件导入成功: ${copiedFile.absolutePath} (${copiedFile.length()} bytes)")
            FileImportResult.Success(copiedFile.absolutePath, fileInfo)
            
        }.getOrElse { exception ->
            Timber.e(exception, "文件导入失败: $uri")
            FileImportResult.Error("文件导入失败: ${exception.message}")
        }
    }
    
    /**
     * 从URI获取文件信息
     */
    private fun getFileInfoFromUri(uri: Uri): FileInfo? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return null
                
                val displayNameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val mimeTypeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                
                val fileName = if (displayNameIndex >= 0) {
                    cursor.getString(displayNameIndex) ?: "unknown.epub"
                } else {
                    "unknown.epub"
                }
                
                val fileSize = if (sizeIndex >= 0) {
                    cursor.getLong(sizeIndex)
                } else {
                    0L
                }
                
                val mimeType = if (mimeTypeIndex >= 0) {
                    cursor.getString(mimeTypeIndex) ?: ""
                } else {
                    ""
                }
                
                FileInfo(
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = mimeType,
                    uri = uri
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "获取文件信息失败")
            null
        }
    }
    
    /**
     * 验证文件
     */
    private fun validateFile(fileInfo: FileInfo): FileValidationResult {
        // 检查文件大小
        if (fileInfo.fileSize > MAX_FILE_SIZE) {
            return FileValidationResult(false, "文件过大，超过100MB限制")
        }
        
        if (fileInfo.fileSize <= 0) {
            return FileValidationResult(false, "文件为空或大小未知")
        }
        
        // 检查文件扩展名
        val fileName = fileInfo.fileName.lowercase()
        if (!fileName.endsWith(".epub")) {
            return FileValidationResult(false, "不支持的文件格式，请选择EPUB文件")
        }
        
        // 检查MIME类型（可选，因为某些系统可能不准确）
        val expectedMimeTypes = setOf(
            EPUB_MIME_TYPE,
            "application/octet-stream",
            "application/zip"
        )
        
        if (fileInfo.mimeType.isNotEmpty() && 
            !expectedMimeTypes.contains(fileInfo.mimeType)) {
            Timber.w("MIME类型可能不匹配: ${fileInfo.mimeType}")
        }
        
        return FileValidationResult(true, "")
    }
    
    /**
     * 创建目标文件
     */
    private fun createTargetFile(fileName: String): File {
        val epubDir = File(context.filesDir, EPUB_CACHE_DIR)
        if (!epubDir.exists()) {
            epubDir.mkdirs()
        }
        
        // 清理文件名，避免特殊字符
        val cleanFileName = fileName.replace(Regex("[^a-zA-Z0-9\u4e00-\u9fa5._-]"), "_")
        
        return File(epubDir, cleanFileName)
    }
    
    /**
     * 复制文件
     */
    private fun copyFile(sourceUri: Uri, targetFile: File): File {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                
                outputStream.flush()
            }
        } ?: throw IOException("无法打开输入流")
        
        return targetFile
    }
    
    /**
     * 删除文件
     */
    suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                Timber.d("文件删除成功: $filePath")
                true
            } else {
                Timber.w("文件不存在或删除失败: $filePath")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "删除文件异常: $filePath")
            false
        }
    }
    
    /**
     * 获取应用EPUB目录
     */
    fun getEpubDirectory(): File {
        val epubDir = File(context.filesDir, EPUB_CACHE_DIR)
        if (!epubDir.exists()) {
            epubDir.mkdirs()
        }
        return epubDir
    }
    
    /**
     * 获取目录大小
     */
    suspend fun getDirectorySize(directory: File): Long = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext 0L
            }
            
            directory.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (e: Exception) {
            Timber.e(e, "计算目录大小失败")
            0L
        }
    }
    
    /**
     * 清理缓存文件
     */
    suspend fun cleanupCache(): CleanupResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val epubDir = getEpubDirectory()
            val totalSize = getDirectorySize(epubDir)
            var deletedCount = 0
            var deletedSize = 0L
            
            // 这里可以实现更复杂的清理策略，比如按时间清理
            // 目前只是示例实现
            
            CleanupResult(
                deletedCount = deletedCount,
                deletedSize = deletedSize,
                totalSize = totalSize
            )
        } catch (e: Exception) {
            Timber.e(e, "清理缓存失败")
            CleanupResult(0, 0L, 0L)
        }
    }
}

/**
 * 文件信息
 */
data class FileInfo(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val uri: Uri
) {
    /**
     * 获取格式化的文件大小
     */
    fun getFormattedSize(): String {
        return when {
            fileSize < 1024 -> "${fileSize}B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024}KB"
            fileSize < 1024 * 1024 * 1024 -> "${fileSize / (1024 * 1024)}MB"
            else -> "${fileSize / (1024 * 1024 * 1024)}GB"
        }
    }
}

/**
 * 文件导入结果
 */
sealed class FileImportResult {
    data class Success(val filePath: String, val fileInfo: FileInfo) : FileImportResult()
    data class Error(val message: String) : FileImportResult()
}

/**
 * 文件验证结果
 */
data class FileValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)

/**
 * 清理结果
 */
data class CleanupResult(
    val deletedCount: Int,
    val deletedSize: Long,
    val totalSize: Long
) {
    fun getFormattedDeletedSize(): String {
        return when {
            deletedSize < 1024 -> "${deletedSize}B"
            deletedSize < 1024 * 1024 -> "${deletedSize / 1024}KB"
            deletedSize < 1024 * 1024 * 1024 -> "${deletedSize / (1024 * 1024)}MB"
            else -> "${deletedSize / (1024 * 1024 * 1024)}GB"
        }
    }
} 