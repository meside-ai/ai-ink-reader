package com.newbiechen.inkreader.application.di

import com.newbiechen.inkreader.core.ai.AIQuestionService
import com.newbiechen.inkreader.core.ai.AIQuestionServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AI Module for Dependency Injection
 * Provides AI-related services for Phase 2 implementation
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {
    
    /**
     * Binds AI Question Service implementation
     */
    @Binds
    @Singleton
    abstract fun bindAIQuestionService(
        aiQuestionServiceImpl: AIQuestionServiceImpl
    ): AIQuestionService
} 