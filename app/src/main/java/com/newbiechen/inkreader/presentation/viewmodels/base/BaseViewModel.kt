package com.newbiechen.inkreader.presentation.viewmodels.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel基类
 * 
 * 提供统一的状态管理、错误处理和协程管理
 */
abstract class BaseViewModel : ViewModel() {
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // 成功消息
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    // 活跃的协程任务
    private val activeJobs = mutableSetOf<Job>()
    
    /**
     * 统一的协程异常处理器
     */
    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.e(exception, "协程执行异常")
        _isLoading.postValue(false)
        handleError(exception)
    }
    
    /**
     * 安全执行协程任务
     */
    protected fun launchSafely(
        showLoading: Boolean = true,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        if (showLoading) {
            _isLoading.value = true
        }
        
        val job = viewModelScope.launch(coroutineExceptionHandler) {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e, "执行任务异常")
                onError?.invoke(e) ?: handleError(e)
            } finally {
                if (showLoading) {
                    _isLoading.postValue(false)
                }
            }
        }
        
        activeJobs.add(job)
        job.invokeOnCompletion { activeJobs.remove(job) }
        
        return job
    }
    
    /**
     * 处理错误
     */
    protected open fun handleError(throwable: Throwable) {
        val errorMessage = when (throwable) {
            is IllegalArgumentException -> throwable.message ?: "参数错误"
            is IllegalStateException -> throwable.message ?: "状态错误"
            is java.io.FileNotFoundException -> "文件不存在"
            is java.io.IOException -> "文件读写错误"
            is java.net.ConnectException -> "网络连接失败"
            is java.net.SocketTimeoutException -> "网络请求超时"
            else -> throwable.message ?: "未知错误"
        }
        
        Timber.w("处理错误: $errorMessage")
        _error.postValue(errorMessage)
    }
    
    /**
     * 设置错误信息
     */
    protected fun setError(message: String) {
        _error.postValue(message)
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.postValue(null)
    }
    
    /**
     * 设置成功消息
     */
    protected fun setSuccessMessage(message: String) {
        _successMessage.postValue(message)
    }
    
    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _successMessage.postValue(null)
    }
    
    /**
     * 设置加载状态
     */
    protected fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }
    
    /**
     * 取消所有活跃任务
     */
    protected fun cancelAllJobs() {
        activeJobs.forEach { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
        activeJobs.clear()
    }
    
    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
        Timber.d("${this::class.simpleName} cleared")
    }
} 