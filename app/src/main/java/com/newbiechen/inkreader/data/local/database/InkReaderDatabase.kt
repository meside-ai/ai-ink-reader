package com.newbiechen.inkreader.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.newbiechen.inkreader.data.local.database.entities.*
import com.newbiechen.inkreader.data.local.database.dao.*
import com.newbiechen.inkreader.data.local.database.converters.DatabaseConverters

/**
 * 墨水屏AI阅读器数据库
 * 
 * 使用Room持久性库管理本地数据存储，包含所有实体和数据访问对象
 */
@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingProgressEntity::class,
        AIConversationEntity::class,
        AIResponseEntity::class
    ],
    version = 1,
    exportSchema = false // MVP版本不导出schema
)
@TypeConverters(DatabaseConverters::class)
abstract class InkReaderDatabase : RoomDatabase() {
    
    /**
     * 图书数据访问对象
     */
    abstract fun bookDao(): BookDao
    
    /**
     * 章节数据访问对象
     */
    abstract fun chapterDao(): ChapterDao
    
    /**
     * 阅读进度数据访问对象
     */
    abstract fun readingProgressDao(): ReadingProgressDao
    
    /**
     * AI对话数据访问对象
     */
    abstract fun aiConversationDao(): AIConversationDao
    
    /**
     * AI回答数据访问对象
     */
    abstract fun aiResponseDao(): AIResponseDao
    
    companion object {
        const val DATABASE_NAME = "ink_reader_database"
        
        /**
         * 创建数据库实例的工厂方法
         * 
         * @param context 应用上下文
         * @param databaseName 数据库名称
         * @return 数据库实例
         */
        fun buildDatabase(
            context: Context,
            databaseName: String = DATABASE_NAME
        ): InkReaderDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                InkReaderDatabase::class.java,
                databaseName
            )
            .fallbackToDestructiveMigration() // MVP版本允许破坏性迁移
            .build()
        }
    }
} 