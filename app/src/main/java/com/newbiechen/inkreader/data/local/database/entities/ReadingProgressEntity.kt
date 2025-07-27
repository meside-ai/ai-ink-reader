package com.newbiechen.inkreader.data.local.database.entities

import androidx.room.*
import java.util.UUID

/**
 * 阅读进度数据库实体类
 * 
 * 跟踪用户在每本书的阅读位置和进度
 */
@Entity(
    tableName = "reading_progress",
    indices = [
        Index(value = ["progress_id"]),
        Index(value = ["book_id"], unique = true), // 每本书只有一个进度记录
        Index(value = ["last_updated"])
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
data class ReadingProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "progress_id")
    val progressId: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "book_id")
    val bookId: String,
    
    @ColumnInfo(name = "chapter_index")
    val chapterIndex: Int = 0,
    
    @ColumnInfo(name = "paragraph_index")
    val paragraphIndex: Int = 0,
    
    @ColumnInfo(name = "character_offset")
    val characterOffset: Int = 0,
    
    @ColumnInfo(name = "progress_percent")
    val progressPercent: Float = 0.0f,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
) 