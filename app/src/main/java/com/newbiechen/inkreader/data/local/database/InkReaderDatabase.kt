package com.newbiechen.inkreader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.newbiechen.inkreader.data.local.database.dao.BookDao
import com.newbiechen.inkreader.data.local.database.dao.ChapterDao
import com.newbiechen.inkreader.data.local.database.dao.AIConversationDao
import com.newbiechen.inkreader.data.local.database.entities.BookEntity
import com.newbiechen.inkreader.data.local.database.entities.ChapterEntity
import com.newbiechen.inkreader.data.local.database.entities.AIConversationEntity

/**
 * 墨水屏阅读器数据库
 * Updated for Phase 2: AI Integration
 */
@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        AIConversationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class InkReaderDatabase : RoomDatabase() {
    
    /**
     * 获取图书DAO
     */
    abstract fun bookDao(): BookDao
    
    /**
     * 获取章节DAO
     */
    abstract fun chapterDao(): ChapterDao
    
    /**
     * 获取AI对话DAO
     */
    abstract fun aiConversationDao(): AIConversationDao
} 