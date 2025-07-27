package com.newbiechen.inkreader.domain.usecases.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use Case基类
 * 
 * 提供统一的用例执行模式，确保所有业务逻辑都在后台线程执行
 */
abstract class UseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * 执行用例
     * 
     * @param parameters 用例参数
     * @return 执行结果
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters).let { Result.success(it) }
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
    
    /**
     * 具体的用例执行逻辑，由子类实现
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * 无参数的Use Case基类
 */
abstract class NoParameterUseCase<R>(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UseCase<Unit, R>(coroutineDispatcher) {
    
    suspend operator fun invoke(): Result<R> {
        return invoke(Unit)
    }
}

/**
 * Flow Use Case基类
 * 
 * 用于返回Flow类型的用例，通常用于观察数据变化
 */
abstract class FlowUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    suspend operator fun invoke(parameters: P) = withContext(coroutineDispatcher) {
        execute(parameters)
    }
    
    protected abstract suspend fun execute(parameters: P): kotlinx.coroutines.flow.Flow<R>
} 