package com.newbiechen.inkreader.domain.usecases.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 基础UseCase抽象类
 * 提供统一的执行框架和错误处理
 */
abstract class UseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * 执行UseCase
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters).let {
                    Result.success(it)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(this::class.simpleName, "UseCase执行失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * UseCase具体实现
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * 无参数UseCase
 */
abstract class UseCase0<R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    suspend operator fun invoke(): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute().let {
                    Result.success(it)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(this::class.simpleName, "UseCase执行失败", e)
            Result.failure(e)
        }
    }
    
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): R
} 