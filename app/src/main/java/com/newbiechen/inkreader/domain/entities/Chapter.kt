package com.newbiechen.inkreader.domain.entities

/**
 * 章节领域实体
 */
data class Chapter(
    val chapterId: String,
    val bookId: String,
    val title: String,
    val content: String,
    val order: Int,
    val wordCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) 