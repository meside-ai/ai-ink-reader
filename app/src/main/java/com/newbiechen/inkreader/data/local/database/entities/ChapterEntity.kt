package com.newbiechen.inkreader.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newbiechen.inkreader.domain.entities.Chapter

/**
 * 章节数据库实体类
 */
@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["chapter_id"]),
        Index(value = ["book_id"]),
        Index(value = ["order"]),
        Index(value = ["created_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey
    @ColumnInfo(name = "chapter_id")
    val chapterId: String,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "order")
    val order: Int,
    
    @ColumnInfo(name = "word_count")
    val wordCount: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entity转换为Domain对象
 */
fun ChapterEntity.toDomain(): Chapter {
    return Chapter(
        chapterId = chapterId,
        bookId = bookId,
        title = title,
        content = content,
        order = order,
        wordCount = wordCount,
        createdAt = createdAt
    )
}

/**
 * Domain对象转换为Entity
 */
fun Chapter.toEntity(): ChapterEntity {
    return ChapterEntity(
        chapterId = chapterId,
        bookId = bookId,
        title = title,
        content = content,
        order = order,
        wordCount = wordCount,
        createdAt = createdAt
    )
} 