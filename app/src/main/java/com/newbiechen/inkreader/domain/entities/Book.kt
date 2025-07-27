package com.newbiechen.inkreader.domain.entities

import java.util.UUID

/**
 * 图书实体类
 * 
 * 表示EPUB图书的核心信息和元数据
 */
data class Book(
    val bookId: String = UUID.randomUUID().toString(),
    val filePath: String,
    val title: String,
    val author: String,
    val publisher: String? = null,
    val language: String = "zh-CN",
    val coverImagePath: String? = null,
    val fileSize: Long = 0L,
    val totalChapters: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    
    /**
     * 获取文件大小的可读格式
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "${fileSize}B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024}KB"
            else -> "${fileSize / (1024 * 1024)}MB"
        }
    }
    
    /**
     * 检查图书是否有效
     */
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               author.isNotBlank() && 
               filePath.isNotBlank() && 
               !isDeleted
    }
    
    /**
     * 获取显示标题（如果标题为空则显示文件名）
     */
    fun getDisplayTitle(): String {
        return if (title.isNotBlank()) {
            title
        } else {
            filePath.substringAfterLast("/").substringBeforeLast(".")
        }
    }
} 