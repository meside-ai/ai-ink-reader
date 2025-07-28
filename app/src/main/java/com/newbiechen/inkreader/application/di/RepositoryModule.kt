package com.newbiechen.inkreader.application.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.newbiechen.inkreader.domain.repositories.BookRepository
import com.newbiechen.inkreader.data.repositories.BookRepositoryImpl

/**
 * Repository依赖注入模块
 * 
 * 绑定Domain层Repository接口与Data层具体实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * 绑定BookRepository实现
     */
    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository
} 