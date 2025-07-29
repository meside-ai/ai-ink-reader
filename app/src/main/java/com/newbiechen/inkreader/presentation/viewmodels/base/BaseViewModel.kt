package com.newbiechen.inkreader.presentation.viewmodels.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 基础ViewModel抽象类
 * 提供统一的错误处理和状态管理
 */
abstract class BaseViewModel : ViewModel() {
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        android.util.Log.e(this::class.simpleName, "ViewModel异常", exception)
        _error.value = exception.message ?: "未知错误"
        _isLoading.value = false
    }
    
    /**
     * 安全执行协程
     */
    protected fun launchSafe(
        showLoading: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                if (showLoading) _isLoading.value = true
                _error.value = null
                block()
            } catch (e: Exception) {
                android.util.Log.e(this@BaseViewModel::class.simpleName, "执行失败", e)
                _error.value = e.message ?: "操作失败"
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 显示错误信息
     */
    protected fun showError(message: String) {
        _error.value = message
    }
    
    /**
     * 显示加载状态
     */
    protected fun showLoading(loading: Boolean) {
        _isLoading.value = loading
    }
} 