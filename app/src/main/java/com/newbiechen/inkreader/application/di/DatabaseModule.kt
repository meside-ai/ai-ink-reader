package com.newbiechen.inkreader.application.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.newbiechen.inkreader.data.local.database.InkReaderDatabase
import com.newbiechen.inkreader.data.local.database.dao.BookDao
import com.newbiechen.inkreader.data.local.database.dao.ChapterDao
import com.newbiechen.inkreader.data.local.database.dao.AIConversationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 * Updated for Phase 2: AI Integration
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Database migration from version 1 to 2
     * Adds AI conversations table
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `ai_conversations` (
                    `id` TEXT NOT NULL,
                    `book_id` TEXT NOT NULL,
                    `book_title` TEXT NOT NULL,
                    `chapter_title` TEXT,
                    `selected_text` TEXT NOT NULL,
                    `question_type` TEXT NOT NULL,
                    `answer` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `is_from_voice` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`book_id`) REFERENCES `books`(`book_id`) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Create index for better query performance
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_conversations_book_id` ON `ai_conversations` (`book_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_conversations_timestamp` ON `ai_conversations` (`timestamp`)")
        }
    }
    
    /**
     * 提供数据库实例
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InkReaderDatabase {
        return Room.databaseBuilder(
            context,
            InkReaderDatabase::class.java,
            "ink_reader_database"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }
    
    /**
     * 提供图书DAO
     */
    @Provides
    fun provideBookDao(database: InkReaderDatabase): BookDao {
        return database.bookDao()
    }
    
    /**
     * 提供章节DAO
     */
    @Provides
    fun provideChapterDao(database: InkReaderDatabase): ChapterDao {
        return database.chapterDao()
    }
    
    /**
     * 提供AI对话DAO
     */
    @Provides
    fun provideAIConversationDao(database: InkReaderDatabase): AIConversationDao {
        return database.aiConversationDao()
    }
} 