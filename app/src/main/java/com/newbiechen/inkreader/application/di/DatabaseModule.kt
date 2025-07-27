package com.newbiechen.inkreader.application.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.newbiechen.inkreader.data.local.database.InkReaderDatabase
import com.newbiechen.inkreader.data.local.database.dao.*

/**
 * 数据库依赖注入模块
 * 
 * 提供Room数据库和相关DAO的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供Room数据库实例
     */
    @Provides
    @Singleton
    fun provideInkReaderDatabase(
        @ApplicationContext context: Context
    ): InkReaderDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = InkReaderDatabase::class.java,
            name = InkReaderDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // MVP版本允许破坏性迁移
        .build()
    }
    
    /**
     * 提供BookDao
     */
    @Provides
    fun provideBookDao(database: InkReaderDatabase): BookDao {
        return database.bookDao()
    }
    
    /**
     * 提供ChapterDao  
     */
    @Provides
    fun provideChapterDao(database: InkReaderDatabase): ChapterDao {
        return database.chapterDao()
    }
    
    /**
     * 提供ReadingProgressDao
     */
    @Provides
    fun provideReadingProgressDao(database: InkReaderDatabase): ReadingProgressDao {
        return database.readingProgressDao()
    }
    
    /**
     * 提供AIConversationDao
     */
    @Provides
    fun provideAIConversationDao(database: InkReaderDatabase): AIConversationDao {
        return database.aiConversationDao()
    }
    
    /**
     * 提供AIResponseDao
     */
    @Provides
    fun provideAIResponseDao(database: InkReaderDatabase): AIResponseDao {
        return database.aiResponseDao()
    }
} 