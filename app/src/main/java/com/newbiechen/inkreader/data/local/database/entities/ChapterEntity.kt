package com.newbiechen.inkreader.data.local.database.entities

import androidx.room.*
import com.newbiechen.inkreader.domain.entities.Chapter

/**
 * 章节数据库实体类
 * 
 * 包含与图书的外键关联和查询优化索引
 */
@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["chapter_id"]),
        Index(value = ["book_id"]), // 外键索引
        Index(value = ["book_id", "chapter_index"], unique = true), // 复合唯一索引
        Index(value = ["chapter_index"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE // 删除图书时级联删除章节
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey
    @ColumnInfo(name = "chapter_id")
    val chapterId: String,
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "chapter_index")
    val chapterIndex: Int,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "resource_path")
    val resourcePath: String,
    
    @ColumnInfo(name = "anchor")
    val anchor: String? = null,
    
    @ColumnInfo(name = "word_count")
    val wordCount: Int = 0,
    
    @ColumnInfo(name = "estimated_reading_time")
    val estimatedReadingTime: Int = 0
)

/**
 * 扩展函数：Entity转换为Domain对象
 */
fun ChapterEntity.toDomain(): Chapter {
    return Chapter(
        chapterId = chapterId,
        bookId = bookId,
        chapterIndex = chapterIndex,
        title = title,
        resourcePath = resourcePath,
        anchor = anchor,
        wordCount = wordCount,
        estimatedReadingTime = estimatedReadingTime
    )
}

/**
 * 扩展函数：Domain对象转换为Entity
 */
fun Chapter.toEntity(): ChapterEntity {
    return ChapterEntity(
        chapterId = chapterId,
        bookId = bookId,
        chapterIndex = chapterIndex,
        title = title,
        resourcePath = resourcePath,
        anchor = anchor,
        wordCount = wordCount,
        estimatedReadingTime = estimatedReadingTime
    )
} 