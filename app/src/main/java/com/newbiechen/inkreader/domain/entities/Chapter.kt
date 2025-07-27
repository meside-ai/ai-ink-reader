package com.newbiechen.inkreader.domain.entities

import java.util.UUID

/**
 * 章节实体类
 * 
 * 表示EPUB图书中的章节信息
 */
data class Chapter(
    val chapterId: String = UUID.randomUUID().toString(),
    val bookId: String,
    val chapterIndex: Int,
    val title: String,
    val resourcePath: String,
    val anchor: String? = null,
    val wordCount: Int = 0,
    val estimatedReadingTime: Int = 0 // 预估阅读时间（分钟）
) {
    
    /**
     * 获取格式化的章节标题
     */
    fun getFormattedTitle(): String {
        return if (title.isNotBlank()) {
            title
        } else {
            "第${chapterIndex + 1}章"
        }
    }
    
    /**
     * 获取预估阅读时间的可读格式
     */
    fun getFormattedReadingTime(): String {
        return when {
            estimatedReadingTime < 1 -> "不到1分钟"
            estimatedReadingTime < 60 -> "${estimatedReadingTime}分钟"
            else -> "${estimatedReadingTime / 60}小时${estimatedReadingTime % 60}分钟"
        }
    }
    
    /**
     * 检查章节是否有效
     */
    fun isValid(): Boolean {
        return bookId.isNotBlank() && 
               resourcePath.isNotBlank() && 
               chapterIndex >= 0
    }
} 