package com.newbiechen.inkreader.application.di

import com.newbiechen.inkreader.core.epub.EpubParserService
import com.newbiechen.inkreader.core.epub.EpubParserServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EpubModule {

    @Binds
    @Singleton
    abstract fun bindEpubParserService(
        epubParserService: EpubParserServiceImpl
    ): EpubParserService
} 