package com.newbiechen.inkreader.core.files

import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件管理服务
 * 负责应用文件的管理和操作
 */
@Singleton
class FileManagerService @Inject constructor(
    private val context: Context
) {
    
    /**
     * 获取应用数据目录
     */
    fun getAppDataDir(): File {
        return context.filesDir
    }
    
    /**
     * 获取图书存储目录
     */
    fun getBooksDir(): File {
        val booksDir = File(getAppDataDir(), "books")
        if (!booksDir.exists()) {
            booksDir.mkdirs()
        }
        return booksDir
    }
    
    /**
     * 获取封面图片存储目录
     */
    fun getCoversDir(): File {
        val coversDir = File(getAppDataDir(), "covers")
        if (!coversDir.exists()) {
            coversDir.mkdirs()
        }
        return coversDir
    }
    
    /**
     * 获取缓存目录
     */
    fun getCacheDir(): File {
        return context.cacheDir
    }
    
    /**
     * 检查文件是否存在
     */
    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
    
    /**
     * 获取文件大小
     */
    fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists()) file.length() else 0L
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            android.util.Log.e("FileManager", "删除文件失败: $filePath", e)
            false
        }
    }
    
    /**
     * 复制文件到应用目录
     */
    fun copyFileToAppDir(sourceFilePath: String, targetFileName: String): Result<String> {
        return try {
            val sourceFile = File(sourceFilePath)
            if (!sourceFile.exists()) {
                return Result.failure(IllegalArgumentException("源文件不存在: $sourceFilePath"))
            }
            
            val targetFile = File(getBooksDir(), targetFileName)
            sourceFile.copyTo(targetFile, overwrite = true)
            
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            android.util.Log.e("FileManager", "复制文件失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
            else -> "${bytes / (1024 * 1024 * 1024)}GB"
        }
    }
    
    /**
     * 清理临时文件
     */
    fun cleanupTempFiles(): Int {
        return try {
            val tempFiles = getCacheDir().listFiles() ?: return 0
            var cleanedCount = 0
            
            tempFiles.forEach { file ->
                if (file.delete()) {
                    cleanedCount++
                }
            }
            
            android.util.Log.d("FileManager", "清理临时文件: $cleanedCount 个")
            cleanedCount
        } catch (e: Exception) {
            android.util.Log.e("FileManager", "清理临时文件失败", e)
            0
        }
    }
} 