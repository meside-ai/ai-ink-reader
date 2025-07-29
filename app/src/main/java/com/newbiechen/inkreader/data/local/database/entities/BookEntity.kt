package com.newbiechen.inkreader.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newbiechen.inkreader.domain.entities.Book

/**
 * 图书数据库实体类
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["book_id"]),
        Index(value = ["file_path"], unique = true),
        Index(value = ["created_at"]),
        Index(value = ["last_opened_at"]),
        Index(value = ["is_deleted"]),
        Index(value = ["author", "title"])
    ]
)
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "file_path")
    val filePath: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "author")
    val author: String,
    
    @ColumnInfo(name = "publisher")
    val publisher: String? = null,
    
    @ColumnInfo(name = "language")
    val language: String = "zh-CN",
    
    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String? = null,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0L,
    
    @ColumnInfo(name = "total_chapters")
    val totalChapters: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_opened_at")
    val lastOpenedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

/**
 * Entity转换为Domain对象
 */
fun BookEntity.toDomain(): Book {
    return Book(
        bookId = bookId,
        filePath = filePath,
        title = title,
        author = author,
        publisher = publisher,
        language = language,
        coverImagePath = coverImagePath,
        fileSize = fileSize,
        totalChapters = totalChapters,
        createdAt = createdAt,
        lastOpenedAt = lastOpenedAt,
        isDeleted = isDeleted
    )
}

/**
 * Domain对象转换为Entity
 */
fun Book.toEntity(): BookEntity {
    return BookEntity(
        bookId = bookId,
        filePath = filePath,
        title = title,
        author = author,
        publisher = publisher,
        language = language,
        coverImagePath = coverImagePath,
        fileSize = fileSize,
        totalChapters = totalChapters,
        createdAt = createdAt,
        lastOpenedAt = lastOpenedAt,
        isDeleted = isDeleted
    )
} 