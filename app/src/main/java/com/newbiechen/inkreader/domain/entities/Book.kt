package com.newbiechen.inkreader.domain.entities

/**
 * 图书领域实体
 */
data class Book(
    val bookId: String,
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
) 