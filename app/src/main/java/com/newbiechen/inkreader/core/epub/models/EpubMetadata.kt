package com.newbiechen.inkreader.core.epub.models

import com.newbiechen.inkreader.domain.entities.Book
import com.newbiechen.inkreader.domain.entities.Chapter
import java.util.UUID

/**
 * EPUB元数据模型
 * 
 * 包含从EPUB文件解析出的完整图书信息
 */
data class EpubMetadata(
    val bookId: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val publisher: String? = null,
    val description: String? = null,
    val language: String = "zh-CN",
    val isbn: String? = null,
    val publicationDate: String? = null,
    val coverImagePath: String? = null,
    val filePath: String,
    val fileSize: Long = 0L,
    val totalChapters: Int = 0,
    val chapters: List<ChapterInfo> = emptyList(),
    val spine: List<String> = emptyList(), // 章节阅读顺序
    val manifest: Map<String, ManifestItem> = emptyMap() // 资源清单
) {
    
    /**
     * 转换为Domain层的Book实体
     */
    fun toBook(): Book {
        return Book(
            bookId = bookId,
            filePath = filePath,
            title = title,
            author = author,
            publisher = publisher,
            language = language,
            coverImagePath = coverImagePath,
            fileSize = fileSize,
            totalChapters = chapters.size,
            createdAt = System.currentTimeMillis(),
            lastOpenedAt = System.currentTimeMillis(),
            isDeleted = false
        )
    }
    
    /**
     * 获取章节列表
     */
    fun getChapters(): List<Chapter> {
        return chapters.mapIndexed { index, chapterInfo ->
            Chapter(
                chapterId = UUID.randomUUID().toString(),
                bookId = bookId,
                chapterIndex = index,
                title = chapterInfo.title,
                resourcePath = chapterInfo.href,
                anchor = chapterInfo.anchor,
                wordCount = chapterInfo.wordCount,
                estimatedReadingTime = chapterInfo.estimatedReadingTime
            )
        }
    }
    
    /**
     * 验证元数据是否有效
     */
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               author.isNotBlank() && 
               filePath.isNotBlank() &&
               chapters.isNotEmpty()
    }
}

/**
 * 章节信息
 */
data class ChapterInfo(
    val title: String,
    val href: String, // 在EPUB中的路径
    val anchor: String? = null, // HTML锚点
    val level: Int = 1, // 章节层级
    val wordCount: Int = 0,
    val estimatedReadingTime: Int = 0 // 分钟
) {
    
    /**
     * 获取清理后的章节标题
     */
    fun getCleanTitle(): String {
        return title.trim()
            .replace(Regex("^第?\\d+章\\s*"), "") // 移除章节编号
            .replace(Regex("^Chapter\\s*\\d+\\s*", RegexOption.IGNORE_CASE), "")
            .ifBlank { "第${level}章" }
    }
}

/**
 * EPUB清单项
 */
data class ManifestItem(
    val id: String,
    val href: String,
    val mediaType: String,
    val properties: List<String> = emptyList()
) {
    
    /**
     * 检查是否为导航文件
     */
    fun isNavigation(): Boolean {
        return mediaType == "application/xhtml+xml" && 
               properties.contains("nav")
    }
    
    /**
     * 检查是否为HTML内容
     */
    fun isHtmlContent(): Boolean {
        return mediaType == "application/xhtml+xml" || 
               mediaType == "text/html"
    }
    
    /**
     * 检查是否为图片资源
     */
    fun isImage(): Boolean {
        return mediaType.startsWith("image/")
    }
    
    /**
     * 检查是否为CSS样式
     */
    fun isStylesheet(): Boolean {
        return mediaType == "text/css"
    }
}

/**
 * EPUB解析结果
 */
sealed class EpubParseResult {
    data class Success(val metadata: EpubMetadata) : EpubParseResult()
    data class Error(val exception: Throwable, val message: String) : EpubParseResult()
}

/**
 * EPUB验证结果
 */
data class EpubValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    
    fun hasErrors(): Boolean = errors.isNotEmpty()
    fun hasWarnings(): Boolean = warnings.isNotEmpty()
    
    fun getAllIssues(): List<String> = errors + warnings
} 